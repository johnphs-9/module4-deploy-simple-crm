package sg.edu.ntu.simple_crm_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.ntu.simple_crm_api.model.Interaction;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
}
