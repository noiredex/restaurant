package choplan.db.application.properties.choplan.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreAddress {
    private String roadAddress;   // 도로명 주소
    private String detailAddress; // 상세 주소
}
