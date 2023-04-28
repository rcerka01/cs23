package cd.dispatch.validation

import cats.data.*
import cats.data.Validated.*
import cats.implicits.*
import zio.test.*
import zio.test.Assertion.*
import cs.dispatch.domain.{ScoredCardResponse, ScoredCardResponseError}
import cats.data.NonEmptyList

object ScoredCardValidationSpec extends ZIOSpecDefault {

  private val validScoredCardResponse = ScoredCardResponse("MyCard", 10.0, 0.8)

  private val invalidAprScoredCardResponse = ScoredCardResponse("MyCard", -10.0, 0.8)
  private val invalidNameScoredCardResponse = ScoredCardResponse("", 10.0, 0.8)
  private val invalidApprovalRatingScoredCardResponse = ScoredCardResponse("MyCard", 10.0, 1.5)

  // todo toString, why??
  def spec =
    suite("ScoredCardResponse")(
      test("validate should return a valid ScoredCardResponse if all fields are correct") {
        assert(ScoredCardResponse.validate(validScoredCardResponse))(equalTo(Valid(validScoredCardResponse)))
      },
      test("validate should return an invalid ScoredCardResponse if the APR is negative") {
        val expectedErrors = NonEmptyList.of(new ScoredCardResponseError("Scored card apr value of -10.0 is smaller than 0. Item eliminated."))
        assert(ScoredCardResponse.validate(invalidAprScoredCardResponse).toString)(equalTo(Validated.Invalid(expectedErrors).toString))
      },
      test("validate should return an invalid ScoredCardResponse if the name is empty") {
        val expectedErrors = NonEmptyList.of(new ScoredCardResponseError("Scored card name is empty. Item eliminated."))
        assert(ScoredCardResponse.validate(invalidNameScoredCardResponse).toString)(equalTo(Invalid(expectedErrors).toString))
      },
      test("validate should return an invalid ScoredCardResponse if the approval rating is greater than 1") {
        val expectedErrors = NonEmptyList.of(new ScoredCardResponseError("Scored card eligibility value of 1.5 is greater than 1. Item eliminated."))
        assert(ScoredCardResponse.validate(invalidApprovalRatingScoredCardResponse).toString)(equalTo(Invalid(expectedErrors).toString))
      },
      test("validate should return an invalid ScoredCardResponse with all errors if multiple fields are incorrect") {
        val invalidScoredCardResponse = ScoredCardResponse("", -10.0, 1.5)
        val expectedErrors = NonEmptyList.of(
          new ScoredCardResponseError("Scored card apr value of -10.0 is smaller than 0. Item eliminated."),
          new ScoredCardResponseError("Scored card eligibility value of 1.5 is greater than 1. Item eliminated."),
          new ScoredCardResponseError("Scored card name is empty. Item eliminated.")
        ).sortBy(_.getMessage)
        val result = ScoredCardResponse.validate(invalidScoredCardResponse).leftMap(_.sortBy(_.getMessage))
        assert(result.toString)(equalTo(Invalid(expectedErrors).toString))
      }
    )
}
