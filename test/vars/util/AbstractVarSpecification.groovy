package vars.util

import org.codehaus.groovy.control.CompilerConfiguration
import spock.lang.Specification

class AbstractVarSpecification extends Specification {

    MockJenkinsGlobals globals = Spy()
    Binding vars = loadAllVars()

    def loadAllVars() {
        Binding vars = new Binding()
        // Load all vars into the binding
        File varsReadme = new File(this.class.getResource("/vars.md").file)
        File varsDir = varsReadme.parentFile
        for (File var : varsDir.listFiles()) {
            if (var.name.endsWith(".groovy")) {
                String functionName = var.name - ".groovy"
                vars.setVariable(functionName, loadVar(var.text))
            }
        }
        return vars
    }

    def <T> T loadVar(Class<T> type) {
        // Convert the var class name to script name
        String scriptName = "/${type.simpleName.uncapitalize()}.groovy"
        Script script = loadVar(this.class.getResource(scriptName).text)
        // Cast to type
        return script.asType(type)
    }

    Script loadVar(String scriptText) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.setScriptBaseClass(DelegatingScript.class.name)
        GroovyShell shell = new GroovyShell(vars, compilerConfiguration)
        DelegatingScript script = shell.parse(scriptText)
        script.setDelegate(this.globals)
        return script
    }

}
