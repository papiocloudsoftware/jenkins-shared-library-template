package vars.util

/**
 * This class is used to provide default implementations of any methods that are called within a var.
 *
 * Var specs can use this for expectations
 */
class MockJenkinsGlobals {

    void echo(String message) {
        println message
    }

    void error(String error) {
        throw new RuntimeException(error)
    }

}
