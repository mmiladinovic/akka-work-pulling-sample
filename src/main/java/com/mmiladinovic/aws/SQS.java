package com.mmiladinovic.aws;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by miroslavmiladinovic on 24/12/14.
 */
public class SQS {
    private final AmazonSQSClient sqs;
    private final String queueUrl;

    public SQS(String queueUrl) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(queueUrl), "queueUrl must be given");
        this.sqs = new AmazonSQSClient(new DefaultAWSCredentialsProviderChain());
        this.queueUrl = queueUrl;
    }

    public List<SQSMessage> readMessages(int batchSize) {
        Preconditions.checkArgument(batchSize > 0 && batchSize <= 10);
        ReceiveMessageRequest r = new ReceiveMessageRequest(queueUrl);
        r.setMaxNumberOfMessages(batchSize);
        return sqs.receiveMessage(r).getMessages().stream()
                .map(s -> new SQSMessage(s.getMessageId(), s.getBody(), s.getReceiptHandle())).collect(Collectors.toList());
    }

    public Optional<SQSMessage> readMessage() {
        return readMessages(1).stream().findFirst();
    }

    public void deleteMessages(List<SQSMessage> messages) {
        if (messages != null) {
            List<DeleteMessageBatchRequestEntry> batch = messages.stream()
                    .map(s -> new DeleteMessageBatchRequestEntry(s.id, s.receiptHandle))
                    .collect(Collectors.toList());
            sqs.deleteMessageBatch(queueUrl, batch);
        }
    }

    public void deleteMessage(SQSMessage message) {
        Preconditions.checkArgument(message != null);
        sqs.deleteMessage(queueUrl, message.receiptHandle);
    }

    public void sendMessages(List<String> messages) {
        sqs.sendMessageBatch(queueUrl,
                messages.stream().limit(10).map(m -> {
                    SendMessageBatchRequestEntry e = new SendMessageBatchRequestEntry();
                    e.setId(UUID.randomUUID().toString());
                    e.setMessageBody(m);
                    return e;
                }).collect(Collectors.toList()));
    }
}
