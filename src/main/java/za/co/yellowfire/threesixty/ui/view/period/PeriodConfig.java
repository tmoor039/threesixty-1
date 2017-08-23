package za.co.yellowfire.threesixty.ui.view.period;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.DateField;
import com.vaadin.ui.renderers.DateRenderer;
import io.threesixty.ui.component.BlankSupplier;
import io.threesixty.ui.component.EntityPersistFunction;
import io.threesixty.ui.component.EntitySupplier;
import io.threesixty.ui.view.TableDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.spring.annotation.PrototypeScope;
import za.co.yellowfire.threesixty.domain.rating.PeriodService;
import za.co.yellowfire.threesixty.domain.user.UserService;
import za.co.yellowfire.threesixty.ui.I8n;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class PeriodConfig {

    @Bean
    EntitySupplier<PeriodModel, Serializable> periodSupplier(final PeriodService periodService) {
        return id -> Optional.of(new PeriodModel(periodService.findById((String) id)));
    }

    @Bean
    BlankSupplier<PeriodModel> blankPeriodSupplier() {
        return PeriodModel::new;
    }

    @Bean
    EntityPersistFunction<PeriodModel> periodPersistFunction(final PeriodService periodService, final UserService userService) {
        return new EntityPersistFunction<PeriodModel>() {
            @Override
            public PeriodModel apply(final PeriodModel period) {
                return new PeriodModel(periodService.save(period.getWrapped(), userService.getCurrentUser()));
            }
        };
    }

    @Bean
    @PrototypeScope
    ListDataProvider<PeriodModel> periodListDataProvider(final PeriodService periodService) {
        List<PeriodModel> list = periodService.getPeriodRepository().findAll().stream().map(PeriodModel::new).collect(Collectors.toList());
        return new ListDataProvider<>(list);
    }

    @Bean
    TableDefinition<PeriodModel> periodTableDefinition() {
        DateRenderer dateRenderer = new DateRenderer("yyyy-MM-dd", "");
        TableDefinition<PeriodModel> tableDefinition = new TableDefinition<>(PeriodEditView.VIEW_NAME);
        tableDefinition.column(DateField.class).withHeading(I8n.Period.Columns.ID).forProperty(PeriodModel.FIELD_ID).identity();
        tableDefinition.column(DateField.class).withHeading(I8n.Period.Columns.START).forProperty(PeriodModel.FIELD_START).renderer(dateRenderer);
        tableDefinition.column(DateField.class).withHeading(I8n.Period.Columns.END).forProperty(PeriodModel.FIELD_END).renderer(dateRenderer);
        tableDefinition.column(DateField.class).withHeading(I8n.Period.Columns.DEADLINE_PUBLISH).forProperty(PeriodModel.FIELD_DEADLINE_PUBLISHED).renderer(dateRenderer);
        tableDefinition.column(DateField.class).withHeading(I8n.Period.Columns.DEADLINE_COMPLETE).forProperty(PeriodModel.FIELD_DEADLINE_COMPLETED).renderer(dateRenderer);
        tableDefinition.column(Boolean.class).withHeading(I8n.Period.Columns.ACTIVE).forProperty(PeriodModel.FIELD_ACTIVE);
        return tableDefinition;
    }
}
