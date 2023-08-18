package common.model.to;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Asce
 */
@Data
public class GetAvailablePiecesTo implements Serializable {
    /**
     * 接口id
     */
    private Long interfaceId;
}
