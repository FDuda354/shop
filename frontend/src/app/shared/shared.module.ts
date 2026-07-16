import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {FormField, FormRoot} from '@angular/forms/signals';
import {RouterModule} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';

import {InputText} from 'primeng/inputtext';
import {Button} from 'primeng/button';
import {Avatar} from 'primeng/avatar';
import {Menu} from 'primeng/menu';
import {Menubar} from 'primeng/menubar';
import {Checkbox} from 'primeng/checkbox';
import {InputNumber} from 'primeng/inputnumber';
import {Password} from 'primeng/password';
import {Toast} from 'primeng/toast';
import {Paginator} from 'primeng/paginator';
import {TableModule} from 'primeng/table';
import {Chip} from 'primeng/chip';
import {Message} from 'primeng/message';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {ToggleSwitch} from 'primeng/toggleswitch';
import {Image} from 'primeng/image';
import {Select} from 'primeng/select';
import {Textarea} from 'primeng/textarea';
import {Tag} from 'primeng/tag';
import {Drawer} from 'primeng/drawer';
import {IconField} from 'primeng/iconfield';
import {InputIcon} from 'primeng/inputicon';
import {DatePicker} from 'primeng/datepicker';
import {AutoFocus} from 'primeng/autofocus';
import {ProgressSpinner} from 'primeng/progressspinner';
import {OverlayBadge} from 'primeng/overlaybadge';
import {Divider} from 'primeng/divider';
import {Tooltip} from 'primeng/tooltip';

import {ContentLoaderComponent} from './components/ui/content-loader/content-loader.component';
import {ProductCardComponent} from './components/product-card/product-card.component';
import {LanguageSwitcherComponent} from './components/language-switcher/language-switcher.component';

@NgModule({
  declarations: [
    ContentLoaderComponent,
    ProductCardComponent,
    LanguageSwitcherComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    TranslatePipe,
    FormField,
    FormRoot,
    InputText,
    Button,
    Avatar,
    Menu,
    Menubar,
    Checkbox,
    InputNumber,
    Password,
    Toast,
    Paginator,
    TableModule,
    Chip,
    Message,
    ConfirmDialog,
    ToggleSwitch,
    Image,
    Select,
    Textarea,
    Tag,
    Drawer,
    IconField,
    InputIcon,
    DatePicker,
    AutoFocus,
    ProgressSpinner,
    OverlayBadge,
    Divider,
    Tooltip,
  ],
  exports: [
    CommonModule,
    RouterModule,
    FormsModule,
    TranslatePipe,
    FormField,
    FormRoot,
    InputText,
    Button,
    Avatar,
    Menu,
    Menubar,
    Checkbox,
    InputNumber,
    Password,
    Toast,
    Paginator,
    TableModule,
    Chip,
    Message,
    ConfirmDialog,
    ToggleSwitch,
    Image,
    Select,
    Textarea,
    Tag,
    Drawer,
    IconField,
    InputIcon,
    DatePicker,
    AutoFocus,
    ProgressSpinner,
    OverlayBadge,
    Divider,
    Tooltip,
    ContentLoaderComponent,
    ProductCardComponent,
    LanguageSwitcherComponent,
  ],
})
export class SharedModule {
}
