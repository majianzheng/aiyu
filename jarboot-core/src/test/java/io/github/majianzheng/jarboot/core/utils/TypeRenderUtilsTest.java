package io.github.majianzheng.jarboot.core.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.Serializable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author majianzheng
 */
@SuppressWarnings({"java:S1874", "java:S1186", "java:S1172"})
public class TypeRenderUtilsTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public class TestClass implements Serializable {
        public int testMethod(int i, boolean b) {
            return 0;
        }

        public void anotherTestMethod() throws NullPointerException {

        }
    }

    @Test
    public void testDrawInterface() {
        assertThat(TypeRenderUtils.drawInterface(TestClass.class), is(equalTo("java.io.Serializable")));
        assertThat(TypeRenderUtils.drawInterface(Serializable.class), is(equalTo("")));
    }

    @Test
    public void testDrawParametersForMethod() throws NoSuchMethodException {
        Class[] classesOfParameters = new Class[2];
        classesOfParameters[0] = int.class;
        classesOfParameters[1] = boolean.class;

        assertThat(TypeRenderUtils.drawParameters(TestClass.class.getMethod("testMethod", classesOfParameters)), is(equalTo("int\nboolean")));
        assertThat(TypeRenderUtils.drawParameters(TestClass.class.getMethod("anotherTestMethod")), is(equalTo("")));

        assertThat(TypeRenderUtils.drawParameters(String.class.getMethod("charAt", int.class)), is(equalTo("int")));
        assertThat(TypeRenderUtils.drawParameters(String.class.getMethod("isEmpty")), is(equalTo("")));
    }

    @Test
    public void testDrawParametersForMethodThrowsException() throws NoSuchMethodException {
        thrown.expect(NoSuchMethodException.class);
        TypeRenderUtils.drawParameters(TestClass.class.getMethod("method"));
    }

    @Test
    public void testDrawParametersForConstructor() throws NoSuchMethodException {
        Class[] classesOfParameters = new Class[3];
        classesOfParameters[0] = char[].class;
        classesOfParameters[1] = int.class;
        classesOfParameters[2] = int.class;

        assertThat(TypeRenderUtils.drawParameters(String.class.getConstructor(classesOfParameters)), is(equalTo("[]\nint\nint")));
        assertThat(TypeRenderUtils.drawParameters(String.class.getConstructor()), is(equalTo("")));
    }

    @Test
    public void testDrawParametersForConstructorThrowsException() throws NoSuchMethodException {
        thrown.expect(NoSuchMethodException.class);
        TypeRenderUtils.drawParameters(TestClass.class.getConstructor());
    }

    @Test
    public void testDrawReturn() throws NoSuchMethodException {
        Class[] classesOfParameters = new Class[2];
        classesOfParameters[0] = int.class;
        classesOfParameters[1] = boolean.class;

        assertThat(TypeRenderUtils.drawReturn(TestClass.class.getMethod("testMethod", classesOfParameters)), is(equalTo("int")));
        assertThat(TypeRenderUtils.drawReturn(TestClass.class.getMethod("anotherTestMethod")), is(equalTo("void")));

        assertThat(TypeRenderUtils.drawReturn(String.class.getMethod("isEmpty")), is(equalTo("boolean")));
    }

    @Test
    public void testDrawReturnThrowsException() throws NoSuchMethodException {
        thrown.expect(NoSuchMethodException.class);
        TypeRenderUtils.drawReturn(TestClass.class.getMethod("method"));
    }

    @Test
    public void testDrawExceptionsForMethod() throws NoSuchMethodException {
        Class[] classesOfParameters = new Class[2];
        classesOfParameters[0] = int.class;
        classesOfParameters[1] = boolean.class;

        assertThat(TypeRenderUtils.drawExceptions(TestClass.class.getMethod("testMethod", classesOfParameters)), is(equalTo("")));
        assertThat(TypeRenderUtils.drawExceptions(TestClass.class.getMethod("anotherTestMethod")), is(equalTo("java.lang.NullPointerException")));

        assertThat(TypeRenderUtils.drawExceptions(String.class.getMethod("getBytes", String.class)), is(equalTo("java.io.UnsupportedEncodingException")));
    }

    @Test
    public void testDrawExceptionsForMethodThrowsException() throws NoSuchMethodException {
        thrown.expect(NoSuchMethodException.class);
        TypeRenderUtils.drawExceptions(TestClass.class.getMethod("method"));
    }

    @Test
    public void testDrawExceptionsForConstructor() throws NoSuchMethodException {
        Class[] classesOfConstructorParameters = new Class[2];
        classesOfConstructorParameters[0] = byte[].class;
        classesOfConstructorParameters[1] = String.class;

        assertThat(TypeRenderUtils.drawExceptions(String.class.getConstructor()), is(equalTo("")));
        assertThat(TypeRenderUtils.drawExceptions(String.class.getConstructor(classesOfConstructorParameters)), is(equalTo("java.io.UnsupportedEncodingException")));
    }

    @Test
    public void testDrawExceptionsForConstructorThrowsException() throws NoSuchMethodException {
        thrown.expect(NoSuchMethodException.class);
        TypeRenderUtils.drawExceptions(TestClass.class.getConstructor());
    }

}