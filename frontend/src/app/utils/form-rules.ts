import {maxLength, minLength, required, SchemaPath, SchemaPathRules} from '@angular/forms/signals';

export function nameAndSlugRules(
  name: SchemaPath<string, SchemaPathRules.Supported>,
  slug: SchemaPath<string, SchemaPathRules.Supported>,
  msg: (key: string) => () => string,
): void {
  required(name, {message: msg('validation.nameRequired')});
  minLength(name, 3, {message: msg('validation.nameMinLength')});
  maxLength(name, 255, {message: msg('validation.nameMaxLength')});
  required(slug, {message: msg('validation.slugRequired')});
  minLength(slug, 3, {message: msg('validation.slugMinLength')});
  maxLength(slug, 255, {message: msg('validation.slugMaxLength')});
}
