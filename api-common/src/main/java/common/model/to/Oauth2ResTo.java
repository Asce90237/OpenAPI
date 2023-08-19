package common.model.to;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Asce
 */
@Data
public class Oauth2ResTo implements Serializable {

    private String access_token;

    private String token_type;

    private int expires_in;

    private String refresh_token;

    private String scope;

    private int created_at;
}
