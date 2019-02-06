/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';
import { switchMap, take } from 'rxjs/operators';
import { RavenAdaptation } from '../../shared/models';
import { AdaptationService } from '../../shared/services/adaptation.service';
import { HawkAppState } from '../hawk-store';

import {
  ActivatedRouteSnapshot,
  Resolve,
  RouterStateSnapshot,
} from '@angular/router';

@Injectable()
export class AdaptationListResolver implements Resolve<RavenAdaptation[]> {
  constructor(
    private adaptationService: AdaptationService,
    private store: Store<HawkAppState>,
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    _: RouterStateSnapshot,
  ): Observable<RavenAdaptation[]> {
    return this.store.pipe(
      take(1),
      switchMap(state =>
        this.adaptationService.getAdaptations(state.config.app.apiBaseUrl),
      ),
    );
  }
}
