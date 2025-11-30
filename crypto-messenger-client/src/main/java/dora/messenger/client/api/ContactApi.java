package dora.messenger.client.api;

import feign.Param;
import feign.RequestLine;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ContactApi {

    @RequestLine("DELETE /contacts/{contactId}")
    CompletableFuture<Void> delete(@Param("contactId") UUID contactId);
}
