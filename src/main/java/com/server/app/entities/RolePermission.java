package com.server.app.entities;

import com.server.app.entities.impl.RolePermissionId;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "permission_id",
            nullable = false
    )
    private Permission permission;
}