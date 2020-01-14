package demo.webauthn.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.NonNull;
import lombok.Value;


// This is the object describing what the 'preregistration/finish'
// API endpoint expects to receive as the POST body.
//
// It describes a single preregistration and a user that the client
// would like to associate with it.

@Value
public class U2fPreregistrationResponse {

    private final String tsvRow;
    private final UserIdentity user;

    @JsonCreator
    public U2fPreregistrationResponse(
            @NonNull @JsonProperty("tsvRow") String tsvRow,
            @NonNull @JsonProperty("user") UserIdentity user
    ) {
        this.tsvRow = tsvRow;
        this.user = user;
    }
}
