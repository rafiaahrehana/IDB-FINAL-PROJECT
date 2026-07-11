import { Directive, Input, TemplateRef, ViewContainerRef, effect } from '@angular/core';
import { RbacService } from '../../core/services/rbac.service';
import { Role, PermissionCode } from '../../core/models/rbac.model';

@Directive({
  selector: '[hasRole]',
  standalone: true,
})
export class HasRoleDirective {
  @Input('hasRole') roles!: Role | Role[];
  private isVisible = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private rbac: RbacService
  ) {
    effect(() => {
      this.rbac.currentRole();
      this.check();
    });
  }

  private check(): void {
    const roles = Array.isArray(this.roles) ? this.roles : [this.roles];
    const hasAccess = this.rbac.hasAnyRole(roles);

    if (hasAccess && !this.isVisible) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.isVisible = true;
    } else if (!hasAccess && this.isVisible) {
      this.viewContainer.clear();
      this.isVisible = false;
    }
  }
}

@Directive({
  selector: '[hasPermission]',
  standalone: true,
})
export class HasPermissionDirective {
  @Input('hasPermission') permissions!: PermissionCode | PermissionCode[];
  private isVisible = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private rbac: RbacService
  ) {
    effect(() => {
      this.rbac.permissions();
      this.check();
    });
  }

  private check(): void {
    const perms = Array.isArray(this.permissions) ? this.permissions : [this.permissions];
    const hasAccess = this.rbac.hasAnyPermission(perms);

    if (hasAccess && !this.isVisible) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.isVisible = true;
    } else if (!hasAccess && this.isVisible) {
      this.viewContainer.clear();
      this.isVisible = false;
    }
  }
}
