import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackTest {
	final static Logger logger = LoggerFactory
			.getLogger(LogbackTest.class);
	
	public static void main(String[] args) {
		logger.debug("test");
	}
}
