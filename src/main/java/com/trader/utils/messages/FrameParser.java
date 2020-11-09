package com.trader.utils.messages;

import com.trader.market.publish.msg.Message;
import com.trader.market.publish.msg.MessageType;
import com.trader.market.publish.msg.TradeMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

/**
 * @author ex
 */
public class FrameParser implements Handler<Buffer> {

    private Buffer _buffer;
    private int _offset;
    private final Handler<AsyncResult<Message<?>>> client;

    public FrameParser(Handler<AsyncResult<Message<?>>> client) {
        this.client = client;
    }

    @Override
    public void handle(Buffer buffer) {
        append(buffer);

        int offset;

        while (true) {
            // set a rewind point. if a failure occurs,
            // wait for the next handle()/append() and try again
            offset = _offset;

            // how many bytes are in the buffer
            int remainingBytes = bytesRemaining();

            // at least 4 bytes
            if (remainingBytes < 4) {
                break;
            }

            // what is the length of the message
            int length = _buffer.getInt(_offset);
            _offset += 4;

            if (remainingBytes - 4 >= length) {
                // we have a complete message
                int readOffset = _offset;
                Message<TradeMessage> rs = new Message<>();
                MessageType msgType = MessageType.valueOf(_buffer.getByte(readOffset));
                readOffset += 1;
                if (msgType != null) {
                    rs.setType(msgType);
                    rs.setTs(_buffer.getLong(readOffset));
                    readOffset += 8;
                    // read message data
                    switch (msgType) {
                        case TRADE_RESULT: {
                            TradeMessage msg = TradeMessage.of(_buffer, readOffset, length);
                            if (msg == null) {
                                client.handle(Future.failedFuture("invalid trade symbol length"));
                            }
                            rs.setData(msg);
                            client.handle(Future.succeededFuture(rs));
                            break;
                        }
                        default: {
                            // ignored
                        }
                    }
                }else {
                    client.handle(Future.failedFuture("invalid msg type"));
                }
                // next package
                _offset += length;
            } else {
                // not enough data: rewind, and wait
                // for the next packet to appear
                _offset = offset;
                break;
            }
        }
    }

    private void append(Buffer newBuffer) {
        if (newBuffer == null) {
            return;
        }

        // first run
        if (_buffer == null) {
            _buffer = newBuffer;

            return;
        }

        // out of data
        if (_offset >= _buffer.length()) {
            _buffer = newBuffer;
            _offset = 0;

            return;
        }

        // very large packet
        if (_offset > 0) {
            _buffer = _buffer.getBuffer(_offset, _buffer.length());
        }
        _buffer.appendBuffer(newBuffer);

        _offset = 0;
    }

    private int bytesRemaining() {
        return (_buffer.length() - _offset) < 0 ? 0 : (_buffer.length() - _offset);
    }
}
