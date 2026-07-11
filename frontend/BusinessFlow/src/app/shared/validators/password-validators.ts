import { AbstractControl, ValidationErrors, FormGroup } from '@angular/forms';

export function passwordsMatchValidator(
  passwordField = 'password',
  confirmField = 'confirmPassword'
) {
  return (group: AbstractControl): ValidationErrors | null => {
    const form = group as FormGroup;
    const password = form.get(passwordField)?.value;
    const confirm = form.get(confirmField)?.value;
    return password === confirm ? null : { passwordsMismatch: true };
  };
}

export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/;
