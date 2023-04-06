package cs.dispatch.config

case class UpstreamResponse(callTypes: List[Call])

case class Call(callType: String, response: String)
