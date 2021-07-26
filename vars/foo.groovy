import org.foo.Bar

def call() {
    String name = Bar.loadName("foo")
    echo "Hello, ${name}!"
}
