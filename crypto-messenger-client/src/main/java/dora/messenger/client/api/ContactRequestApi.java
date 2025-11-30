package dora.messenger.client.api;

import dora.messenger.protocol.contact.ContactRequestDto;
import dora.messenger.protocol.contact.CreateContactRequest;
import dora.messenger.protocol.contact.UpdateContactRequest;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ContactRequestApi {

    @RequestLine("POST /contact-requests")
    @Headers("Content-Type: application/json")
    CompletableFuture<@NotNull ContactRequestDto> create(@NotNull CreateContactRequest createContactRequest);

    @RequestLine("DELETE /contact-requests/{requestId}")
    CompletableFuture<Void> cancel(@Param("requestId") @NotNull UUID requestId);

    @RequestLine("PATCH /contact-requests/{requestId}")
    @Headers("Content-Type: application/json")
    CompletableFuture<Void> update(
        @Param("requestId") @NotNull UUID requestId,
        @NotNull UpdateContactRequest updateContactRequest
    );
}
