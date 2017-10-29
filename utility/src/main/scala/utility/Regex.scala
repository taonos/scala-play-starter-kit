package utility

object Regex {

  final val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""

  /**
    * ^                 # start-of-string
    * (?=.*[0-9])       # a digit must occur at least once
    * (?=.*[a-z])       # a lower case letter must occur at least once
    * (?=.*[A-Z])       # an upper case letter must occur at least once
    * (?=\S+$)          # no whitespace allowed in the entire string
    * .{8,}             # anything, at least eight places though
    * $                 # end-of-string
    */
  final val passwordRegex = """^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\S+$).{8,}$"""

  /**
    * Username regex with following rules:
    * 1. length >= 3
    * 2. Valid characters: a-z, A-Z, 0-9, points, dashes and underscores.
    */
  final val usernameRegex = """^[a-zA-Z0-9._-]{3,}$"""
}
