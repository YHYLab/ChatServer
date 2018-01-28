import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;


public class TestFixByBiteSize {

	public static void main(String[] args) {
		TestFixByBiteSize tx = new TestFixByBiteSize();
		String text = "안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요";
		String text2 = "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq";
		
		System.out.println(text.length());
		System.out.println(text2.length());
		text = tx.fixByBiteSize("UTF-8", text, 48);
		text2 = tx.fixByBiteSize("UTF-8", text2, 48);
		System.out.println(text);
		System.out.println(text.length());
		System.out.println(text2);
		System.out.println(text2.length());
	}
	
	public String fixByBiteSize(String encoding, String data, int maxBytes) 
	{
	    if (data == null || data.length() == 0 || maxBytes < 1) {
	        return "";
	    }
	    Charset CS = Charset.forName(encoding);
	    CharBuffer cb = CharBuffer.wrap(data);
	    ByteBuffer bb = ByteBuffer.allocate(maxBytes);
	    CharsetEncoder enc = CS.newEncoder();
	    enc.encode(cb, bb, true);
	    bb.flip();
	    return CS.decode(bb).toString();
	}
}
