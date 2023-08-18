package common.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName auth
 */
@Data
public class Auth implements Serializable {
    /**
     * 用户id
     */
    private Long userid;

    /**
     * secretKey
     */
    private String secretkey;

    private static final long serialVersionUID = 1L;
}