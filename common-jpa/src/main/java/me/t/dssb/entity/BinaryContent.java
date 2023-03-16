package me.t.dssb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "binary_content")
public class BinaryContent {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private byte[] fileAsArraysOfBytes;
}
