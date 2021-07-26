package org.foo

import spock.lang.Specification

class BarSpec extends Specification {

    def "can get names from configuration"() {
        expect:
        Bar.loadName("foo") == "World"
        Bar.loadName("bar") == "foo"
    }

}
