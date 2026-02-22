import fi.iki.elonen.NanoHTTPD;
public class test_nano {
    public void method() {
        NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse("ok");
        r.setGzipEncoding(false);
    }
}
