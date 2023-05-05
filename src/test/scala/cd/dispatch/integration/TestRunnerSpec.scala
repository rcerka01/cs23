//package cd.dispatch.integration

//import zio.Scope
//import zio.test.TestAspect.{forked, sequential}
//import zio.test.{Spec, TestEnvironment, ZIOSpecDefault}
//object TestRunnerSpec extends ZIOSpecDefault {
//  override def spec: Spec[TestEnvironment & Scope, Any] =
//    suite("ara") (
//      CSCardsTimeoutSpec.spec,
//      ScoredCardsTimeoutSpec.spec,
//      RecommendationsErrorSpec.spec
//    ) @@ forked
//}
