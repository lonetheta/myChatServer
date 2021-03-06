package com.tao.auth.core.global;

/**
 * Created by michael on 17-7-3.
 */

import com.google.protobuf.Message;
import com.tao.auth.core.handler.MsgHandler;
import com.tao.auth.core.handler.messageHandler.CLoginHandler;
import com.tao.auth.core.handler.messageHandler.CRegisterHandler;
import com.tao.auth.core.handler.messageHandler.GreetHandler;
import com.tao.protobuf.analysis.ParseMap;
import com.tao.protobuf.message.client2server.auth.Auth;
import com.tao.protobuf.message.internal.Internal;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler管理器.
 */
public class HandlerManager {

    private static final Logger logger = LoggerFactory.getLogger(HandlerManager.class);

    /**
     * 保存msgHandler消息处理器的map.
     * key = msg对应的ptoNum的值, value = 对应的MsgHandler的构造函数对象
     */
    private static final Map<Integer, Constructor<? extends MsgHandler>> msgHandlerMap = new HashMap<>();


    /**
     * 向HandlerManager中注册消息以及对应的MsgHandler.
     * @param messageClazz
     * @param msgHandlerClazz
     */
    public static void register(Class<? extends Message> messageClazz, Class<? extends MsgHandler> msgHandlerClazz) {

        //首先获得ptoNum
        int ptoNum = ParseMap.getPtoNum(messageClazz);
        //获得构造函数
        try {
            //获得构造函数
            Constructor<? extends MsgHandler> constructor = msgHandlerClazz.getConstructor(String.class,
                     long.class, Message.class, ChannelHandlerContext.class);
            //添加到msgHandlerMap中
            msgHandlerMap.put(ptoNum, constructor);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException("没有找到对应的构造函数", e);
        }

    }


    /**
     * 获得对应的MsgHandler消息处理器对象.
     * @param ptoNum
     * @param userId
     * @param netId
     * @param message
     * @param ctx
     * @return
     */
    public static MsgHandler getMsgHandler(int ptoNum, String userId, long netId,
                                           Message message, ChannelHandlerContext ctx)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<? extends MsgHandler> constructor = msgHandlerMap.get(ptoNum);
        if(constructor == null) {
            //没有
            logger.info("msgHAndler not exist, ptoNum : {}", ptoNum);
            return null;
        }
        //有
        return constructor.newInstance(userId, netId, message, ctx);
    }


    /**
     * 初始化各类消息的消息处理器.
     */
    public static void initMsgHandlers() {

        logger.info("初始化消息处理器...");

        HandlerManager.register(Internal.Greet.class, GreetHandler.class);
        HandlerManager.register(Auth.CRegister.class, CRegisterHandler.class);
        HandlerManager.register(Auth.CLogin.class, CLoginHandler.class);
        //HandlerManager.register(Chat.CChatMsg.class, CChatMsgHandler.class);

        logger.info("消息处理器初始化完毕.");

    }


}














