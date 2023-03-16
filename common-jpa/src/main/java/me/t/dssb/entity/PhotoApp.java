package me.t.dssb.entity;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "photo_app")
public class PhotoApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telegramFileId;

    private Integer fileSize;

    @OneToOne
    private BinaryContent binaryContent;
}