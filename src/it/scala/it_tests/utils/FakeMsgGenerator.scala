package it_tests.utils
import fabricator._
import consumer._

object FakeMsgGenerator {
  def generateNFakeMsgs(n: Int): List[Msg] = {

    Stream
      .continually()
      .map(_ => {
        val text = Words().sentence
        val desc = Words().sentence
        val title = Words().paragraph
        val ts = fabricator.Alphanumeric().randomLong
        val userId = fabricator.Alphanumeric().randomInt
        val name = Contact().firstName
        val address = Contact().address

        val user = User(userId, name, address)
        Msg(text, desc, title, ts, user)

      })
      .take(n)
      .toList
  }
}
