package test.integration.org.testinfected.support.middlewares;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testinfected.support.*;
import org.testinfected.support.middlewares.ConnectionScope;
import org.testinfected.support.simple.SimpleServer;
import test.support.org.testinfected.support.web.HttpRequest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.jmock.Expectations.same;
import static test.support.org.testinfected.support.web.HttpRequest.aRequest;

// todo Consider rewriting as unit test now that we can mock requests and responses
@RunWith(JMock.class)
public class ConnectionScopeTest {

    Mockery context = new JUnit4Mockery();
    DataSource dataSource = context.mock(DataSource.class);
    Connection connection = context.mock(Connection.class);
    Application app = context.mock(Application.class);

    States connectionStatus = context.states("connection").startsAs("closed");

    ConnectionScope connectionScope = new ConnectionScope(dataSource);

    Server server = new SimpleServer(9999);
    HttpRequest request = aRequest().to(server);

    @Before public void
    startServer() throws Exception {
        context.checking(new Expectations() {{
            allowing(dataSource).getConnection(); will(returnValue(connection)); when(connectionStatus.is("closed")); then(connectionStatus.is("opened"));
            oneOf(connection).close(); when(connectionStatus.is("opened")); then(connectionStatus.is("closed"));
        }});

        server.run(new MiddlewareStack() {{
            use(connectionScope);
            run(app);
        }});
    }

    @After public void
    stopServer() throws Exception {
        server.shutdown();
    }

    @Test public void
    makesConnectionAvailableToNextApplication() throws Exception {
        context.checking(new Expectations() {{
            oneOf(app).handle(with(aRequestWithAttribute(Connection.class, sameConnection(connection))), with(any(Response.class))); when(connectionStatus.is("opened"));
        }});

        request.send();
    }

    @Test public void
    gracefullyClosesConnectionWhenAnErrorOccurs() throws Exception {
        context.checking(new Expectations() {{
            allowing(app).handle(with(any(Request.class)), with(any(Response.class))); will(throwException(new Exception("error")));
        }});

        request.send();
    }

    private Matcher<Object> sameConnection(final Connection connection) {
        return same((Object) connection);
    }

    private Matcher<Request> aRequestWithAttribute(final Object attribute, Matcher<Object> connection) {
        return new FeatureMatcher<Request, Object>(connection, "a request with attribute " + attribute, attribute.toString()) {
            protected Object featureValueOf(Request actual) {
                return actual.attribute(attribute);
            }
        };
    }
}