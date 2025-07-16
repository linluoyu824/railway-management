/**
 * This package-info file defines a global Hibernate filter for multi-tenancy.
 * The 'tenantFilter' will be used to automatically filter entities based on the departmentId.
 */
@org.hibernate.annotations.FilterDef(
        name = "tenantFilter",
        parameters = @org.hibernate.annotations.ParamDef(name = "departmentId", type = Long.class),
        defaultCondition = "department_id = :departmentId"
)
package com.railway.managementsystem.department.model;