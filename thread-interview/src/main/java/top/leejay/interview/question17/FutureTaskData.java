package top.leejay.interview.question17;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
public class FutureTaskData extends FutureTask<RealData> implements Data {

    public FutureTaskData(Callable<RealData> callable) {
        super(callable);
    }

    @Override
    public String getContent() throws ExecutionException {
        String content = null;
        try {
            content = get().getContent();
        } catch (InterruptedException e) {
        }
        return content;
    }
}
