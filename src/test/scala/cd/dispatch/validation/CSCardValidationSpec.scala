package cs.dispatch.domain

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import zio.test.*
import zio.test.Assertion.*

object CSCardValidationSpec extends ZIOSpecDefault {

  private val validCSCardResponse = CSCardResponse(10.0, "MyCard", 0.8)

  private val invalidAprCSCardResponse = CSCardResponse(-10.0, "MyCard", 0.8)
  private val invalidNameCSCardResponse = CSCardResponse(10.0, "", 0.8)
  private val invalidEligibilityCSCardResponse =
    CSCardResponse(10.0, "MyCard", 11.0)

  def spec = suite("CSCardResponse")(
    test(
      "validate should return a valid CSCardResponse if all fields are correct"
    ) {
      assert(CSCardResponse.validate(validCSCardResponse).toString)(
        equalTo(Valid(validCSCardResponse).toString)
      )
    },
    test(
      "validate should return an invalid CSCardResponse if the APR is negative"
    ) {
      val expectedErrors = NonEmptyList.of(
        new CSCardResponseError(
          "CS Card apr value of -10.0 is smaller than 0. Item eliminated."
        )
      )
      assert(CSCardResponse.validate(invalidAprCSCardResponse).toString)(
        equalTo(Invalid(expectedErrors).toString)
      )
    },
    test(
      "validate should return an invalid CSCardResponse if the name is empty"
    ) {
      val expectedErrors = NonEmptyList.of(
        new CSCardResponseError("CS Card name is empty. Item eliminated.")
      )
      assert(CSCardResponse.validate(invalidNameCSCardResponse).toString)(
        equalTo(Invalid(expectedErrors).toString)
      )
    },
    test(
      "validate should return an invalid CSCardResponse if the eligibility is greater than 10"
    ) {
      val expectedErrors = NonEmptyList.of(
        new CSCardResponseError(
          "CS Card eligibility value of 11.0 is greater than 10. Item eliminated."
        )
      )
      assert(
        CSCardResponse.validate(invalidEligibilityCSCardResponse).toString
      )(equalTo(Invalid(expectedErrors).toString))
    },
    test(
      "validate should return an invalid CSCardResponse with all errors if multiple fields are incorrect"
    ) {
      val invalidCSCardResponse = CSCardResponse(-10.0, "", 11.0)
      val expectedErrors = NonEmptyList
        .of(
          new CSCardResponseError(
            "CS Card apr value of -10.0 is smaller than 0. Item eliminated."
          ),
          new CSCardResponseError(
            "CS Card eligibility value of 11.0 is greater than 10. Item eliminated."
          ),
          new CSCardResponseError("CS Card name is empty. Item eliminated.")
        )
        .sortBy(_.getMessage)
      val result = CSCardResponse
        .validate(invalidCSCardResponse)
        .leftMap(_.sortBy(_.getMessage))
      assert(result.toString)(equalTo(Invalid(expectedErrors).toString))
    }
  )
}
