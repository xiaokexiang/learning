package top.leejay.datasource;

import io.spring.database.datasource.annotation.ConditionalOnDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class ConditionalOnDriverName implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Optional<Map<String, Object>> optional = Optional.ofNullable(annotatedTypeMetadata.getAnnotationAttributes(ConditionalOnDriver.class.getName()));
        if (!optional.isPresent()) {
            log.error("[No ConditionalOnDriver.class exist in BeanFactory]");
            return false;
        }
        try {
            Class.forName((String) optional.get().get("value"));
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
