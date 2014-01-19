import java.nio.*
import java.nio.file.*
import java.nio.charset.*

def BUFFER_SIZE = 2048
def INIT_SHOW_LINES = 10
def ENCODING = System.getProperty("file.encoding")
def CRLF = System.getProperty("line.separator");
def POLLING_INTERVAL = 1000

def eb = vertx.eventBus

def FILENAME = container.getConfig().get("filename")
def PORT = container.getConfig().get("port")

vertx.createHttpServer().websocketHandler { ws ->
	eb.registerHandler("update", { message ->
	  ws.writeTextFrame(message.body())
	})
}.requestHandler { req ->
	if (req.uri == "/") req.response.sendFile "console.html"
}.listen(PORT)

def path = Paths.get(FILENAME)
def sbc = Files.newByteChannel(path, StandardOpenOption.READ)
def lastsize = 0
def init = true
def buf = ByteBuffer.allocate(BUFFER_SIZE)

def timerID = vertx.setPeriodic(POLLING_INTERVAL) { timerID ->
	size = sbc.size()
	def sb = new StringBuilder()
	//println "Size: ${size}"
	if( size > lastsize ) {
		def content
		if (init) { // clip specific tails at startup
			if (size < BUFFER_SIZE) {
				sbc.position(0)
			} else {
				sbc.position(size - BUFFER_SIZE)
			}
			content = read(sbc, buf, ENCODING)
			def lines = content.readLines()
			def maxLineNo = lines.size()
			if (maxLineNo > INIT_SHOW_LINES) {
				content = lines.subList(maxLineNo - INIT_SHOW_LINES, maxLineNo)
				               .join(CRLF) + CRLF
			}
			init = false
		} else {
			content = read(sbc, buf, ENCODING)			
		}
		print content
		eb.publish("update", content)
	} else if ( size < lastsize ) {
		println "<<file is truncated>>"
		eb.publish("update", "<<truncated>>")
		sbc.position(0)
		def content = read(sbc, buf, ENCODING)
		print content
		eb.publish("update", content)
	}
	lastsize = size
	buf.clear()
}

String read(sbc, buf, encoding){
	def sb = new StringBuilder()
	while(sbc.read(buf) > 0) {
		buf.flip()
		sb << Charset.forName(encoding).decode(buf).toString()
	}	
	return sb.toString()
}
