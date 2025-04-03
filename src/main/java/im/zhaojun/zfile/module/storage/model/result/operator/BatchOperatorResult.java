package im.zhaojun.zfile.module.storage.model.result.operator;

import lombok.Data;

/**
 * 批量操作结果
 *
 * @author zhaojun
 */
@Data
public class BatchOperatorResult {

    private String name;

    private String path;

    private boolean success;

    private String message;

    public static BatchOperatorResult success(String name, String path) {
        BatchOperatorResult batchOperatorResult = new BatchOperatorResult();
        batchOperatorResult.setSuccess(true);
        batchOperatorResult.setName(name);
        batchOperatorResult.setPath(path);
        return batchOperatorResult;
    }

    public static BatchOperatorResult fail(String name, String path, String message) {
        BatchOperatorResult batchOperatorResult = new BatchOperatorResult();
        batchOperatorResult.setSuccess(false);
        batchOperatorResult.setName(name);
        batchOperatorResult.setPath(path);
        batchOperatorResult.setMessage(message);
        return batchOperatorResult;
    }

}
