import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BureauComponent } from './bureau.component';

const routes: Routes = [
  {
    path: '',
    component: BureauComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BureauRoutingModule {}
