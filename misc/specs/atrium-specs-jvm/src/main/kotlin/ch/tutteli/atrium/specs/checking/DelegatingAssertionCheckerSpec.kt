package ch.tutteli.atrium.specs.checking

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.internal.expect
import ch.tutteli.atrium.assertions.Assertion
import ch.tutteli.atrium.assertions.AssertionGroup
import ch.tutteli.atrium.assertions.InvisibleAssertionGroupType
import ch.tutteli.atrium.checking.AssertionChecker
import ch.tutteli.atrium.creating.AssertionHolder
import ch.tutteli.atrium.creating.AssertionPlant
import ch.tutteli.atrium.specs.AssertionVerb
import ch.tutteli.atrium.specs.describeFunTemplate
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.Suite

//TODO #116 migrate spek1 to spek2 - move to specs-common
abstract class DelegatingAssertionCheckerSpec(
    testeeFactory: (AssertionHolder) -> AssertionChecker,
    describePrefix: String = "[Atrium] "
) : Spek({

    fun describeFun(vararg funName: String, body: Suite.() -> Unit) =
        describeFunTemplate(describePrefix, funName, body = body)

    val assertions = ArrayList<Assertion>()
    assertions.add(object : Assertion {
        override fun holds() = true
    })
    val assertionVerb = AssertionVerb.VERB
    val assertionWhichFails = object : Assertion {
        override fun holds() = false
    }
    val assertionWhichHolds = object : Assertion {
        override fun holds() = true
    }

    describeFun("check") {
        context("empty assertion list") {
            it("does not throw an exception") {
                val testee = testeeFactory(spyk())
                testee.check(assertionVerb, 1, listOf())
            }
        }

        mapOf(
            "one assertion which fails" to listOf(assertionWhichFails),
            "one assertion which holds" to listOf(assertionWhichHolds),
            "one assertion which fails and one which holds" to listOf(assertionWhichFails, assertionWhichHolds),
            "one assertion which holds and one which fails" to listOf(assertionWhichHolds, assertionWhichFails)
        ).forEach { (description, assertions) ->
            context(description) {
                it("adds the assertion(s) to the subject plant") {
                    //arrange
                    val subjectFactory = spyk<AssertionPlant<Int>>()
                    val testee = testeeFactory(subjectFactory)
                    //act
                    testee.check(assertionVerb, 1, assertions)
                    //assert
                    val captor = slot<Assertion>()
                    verify(exactly = 1) { subjectFactory.addAssertion(assertion = capture(captor)) }
                    expect(captor.captured).isA<AssertionGroup> {
                        feature(AssertionGroup::type).isA<InvisibleAssertionGroupType>()
                        feature(AssertionGroup::assertions) {
                            contains.inAnyOrder.only.values(assertions.first(), *assertions.drop(1).toTypedArray())
                        }
                    }
                }
            }
        }
    }
})
