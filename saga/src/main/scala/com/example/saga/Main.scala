package com.example.saga

import java.util.UUID

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardingEnvelope}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.management.scaladsl.AkkaManagement
import akka.persistence.typed.PersistenceId
import akka.stream.ActorMaterializer
import com.datastax.driver.core.{Cluster, Session}
import com.example.graph.GraphNodeEntity
import com.example.graph.GraphNodeEntity.{GraphNodeCommand, GraphNodeCommandReply, NodeId, TargetNodeId}
import com.example.saga.SagaActor.{NodeReferral, SagaActorReply}
import com.example.saga.config.SagaConfig
import com.example.saga.http.{CORSHandler, RequestApi}
import com.example.user.UserNodeEntity
import com.example.user.UserNodeEntity.{UserCommand, UserId, UserReply}
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  val conf = ConfigFactory.load()
  val config = conf.as[SagaConfig]("SagaConfig")

  import akka.actor.typed.scaladsl.adapter._

  implicit val typedSystem: ActorSystem[SagaCommand] = ActorSystem(Main(), "RayDemo")
  implicit val classSystem = typedSystem.toClassic
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val ec: ExecutionContextExecutor = typedSystem.executionContext
  AkkaManagement(classSystem).start()

  implicit val session: Session = Cluster.builder
    .addContactPoint(config.cassandraConfig.contactPoints)
    .withPort(config.cassandraConfig.port)
    .build
    .connect()

  val sharding = ClusterSharding(typedSystem)

  val settings = ClusterShardingSettings(typedSystem)

  val graphShardRegion: ActorRef[ShardingEnvelope[GraphNodeCommand[GraphNodeCommandReply]]] =
    sharding.init(Entity(GraphNodeEntity.TypeKey)(
      createBehavior = entityContext =>
        GraphNodeEntity.nodeEntityBehaviour(
          PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)
        )
    ).withRole("graph"))

  val userShardRegion: ActorRef[ShardingEnvelope[UserCommand[UserReply]]] =
    sharding.init(Entity(UserNodeEntity.TypeKey)(
      createBehavior = entityContext =>
        UserNodeEntity.userEntityBehaviour(
          PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)
        )
    ).withRole("user"))


  sealed trait SagaCommand
  case class NodeReferralCommand(nodeId: NodeId, userId: UserId, userLabels: Set[String], replyTo: ActorRef[SagaActorReply]) extends SagaCommand

  def apply(): Behavior[SagaCommand] = Behaviors.setup{ context =>
    Behaviors.receiveMessage{
      case referral: NodeReferralCommand =>
        val sagaActor = context.spawn(SagaActor(graphShardRegion, userShardRegion), UUID.randomUUID().toString)
        sagaActor ! NodeReferral(referral.nodeId, referral.userId, referral.userLabels, referral.replyTo)
        Behaviors.same
    }
  }

  val route: Route = RequestApi.route(graphShardRegion)

  private val cors = new CORSHandler {}

  Http().bindAndHandle(
    cors.corsHandler(route),
    config.http.interface,
    config.http.port
  )

}