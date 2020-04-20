package fr.il_totore.ucp

import fr.il_totore.ucp.CommandResult._
import fr.il_totore.ucp.CommandSpec.ImplicitSpec
import fr.il_totore.ucp.parsing.CommandElement.SequenceElement
import fr.il_totore.ucp.parsing.EndElement._
import fr.il_totore.ucp.parsing.ParsingResult._
import fr.il_totore.ucp.parsing.{CommandElement, ParsingResult}
import fr.il_totore.ucp.registration.{CommandRegistry, PrefixedCommandRegistry}
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable.ListBuffer

class CommandTest extends AnyFlatSpec {


  def commandTest(): CommandSpec[String] = {
    def dummyExecutor(sender: String, context: CommandContext[String]): CommandResult = {
      if (!context.getFirst[Boolean]("boolArg").getOrElse(false)) return FAILURE.asCommandResult() whilst "asserting true"
      SUCCESS.asCommandResult() whilst Math.pow(context.getFirst[Int]("intArg").get.toDouble, 2).toString
    }

    val aPermission: String => Boolean = name => name.equals("Il_totore")
    val firstArg: CommandElement[String] = "boolArg" casting(_.toBoolean)
    val secondArg: CommandElement[String] = "intArg" casting(_.toInt) orElse 0
    "myCommand" describedAs "A test command" withPermission aPermission executing dummyExecutor requiring(firstArg and secondArg)
  }

  def createRegister(spec: CommandSpec[String]): CommandRegistry[String] = {
    val registry: CommandRegistry[String] = new PrefixedCommandRegistry(ListBuffer(), "/")
    registry.register(spec)
    registry
  }

  def executeTest(registry: CommandRegistry[String], sender: String, cmd: String): CommandResult = {
    val result: ParsingResult[String] = registry.parse(sender, cmd)
    assert(result.getResultType == SUCCESS)
    assert(result.getContext.isDefined)
    result.getContext.get.execute(sender)
  }


  "A CommandRegistry" should "register and parse commands consistently" in {
    var result = executeTest(createRegister(commandTest()), "Il_totore", "/myCommand false")
    assert(result.equals(FAILURE.asCommandResult() whilst "asserting true"))
    result = executeTest(createRegister(commandTest()), "Il_totore", "/myCommand true 5")
    assert(result.equals(SUCCESS.asCommandResult() whilst "25.0"))
  }
}