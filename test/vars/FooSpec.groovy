package vars

import org.foo.Bar
import vars.util.AbstractVarSpecification

/**
 * Specification for vars/foo.groovy
 *
 * Must extend AbstractVarSpecification and define an interface that defines the methods
 * in the var.
 */
class FooSpec extends AbstractVarSpecification {

    static interface Foo {
        def call()
    }

    Foo foo = loadVar(Foo)

    def "prints 'Hello, <name>!' based on results of Bar.loadName"() {
        given:
        GroovyMock(Bar, global: true)

        when:
        foo()

        then: // Mock the results of Bar.loadName
        1 * Bar.loadName("foo") >> "Bar"

        then: // Expect Hello, Bar! to be printed
        1 * globals.echo("Hello, Bar!")
    }

}
