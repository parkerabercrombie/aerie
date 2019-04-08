/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { ActivityInstance } from '../../shared/models';

export interface ActivityInstanceSvg extends ActivityInstance {
  fill: string;
  height: number;
  labelFill: string;
  labelFontFamily: string;
  labelFontSize: number;
  labelX: number;
  labelY: number;
  showLabel: boolean;
  stroke: string;
  width: number;
  x: number;
  y: number;
}