package com.example.graph.http

import com.example.graph.GraphNodeEntity._

object Requests {
  case class CreateNodeReq(nodeId: String, nodeType: String, properties: Map[String, String] = Map.empty, tags: Tags = Map.empty)
  case class UpdateNodeReq(properties: Map[String, String], tags: Tags = Map.empty)
  case class RemoveEdgeReq(targetNodeId: String, edgeType: String)
  case class UpdateEdgeReq(targetNodeId: String, edgeType: String, direction: String = "to", userId: String, properties: EdgeProperties = Map.empty)
  case class EdgeQueryReq(edgeType: EdgeType, properties: EdgeProperties)

  case class QueryReq(nodeType: Option[NodeType], edgeType: Set[EdgeType], nodeProperties: NodeProperties = Map.empty, edgeProperties: EdgeProperties = Map.empty)
  case class GraphQueryReq(queries: List[QueryReq])
}
