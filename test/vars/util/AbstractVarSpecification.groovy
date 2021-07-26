package vars.util

import org.codehaus.groovy.control.CompilerConfiguration
import spock.lang.Specification

class AbstractVarSpecification extends Specification {

    MockJenkinsGlobals globals = Spy()

    def <T> T loadVar(Class<T> type) {
        // Convert the var class name to script name
        String scriptName = "/${type.simpleName.uncapitalize()}.groovy"
        // Create shell for script delegating to the MockJenkinsGlobals instance
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.setScriptBaseClass(DelegatingScript.class.name)
        GroovyShell shell = new GroovyShell(compilerConfiguration)
        DelegatingScript script = shell.parse(this.class.getResource(scriptName).text)
        script.setDelegate(this.globals)
        // Cast to type
        return script.asType(type)
    }

}
