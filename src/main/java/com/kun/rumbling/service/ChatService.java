package com.kun.rumbling.service;

import com.kun.rumbling.domain.ChatMessage;

public interface ChatService {

    public void processMessage(final ChatMessage chatMessage);

    public void processChatGptMessage(ChatMessage chatMessage);

    public void sendMsg(ChatMessage message);





}
