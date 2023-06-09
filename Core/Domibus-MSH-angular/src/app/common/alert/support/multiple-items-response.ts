/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the list
 * */
export interface MultipleItemsResponse {
  message: string;
  issues: ResponseItemDetail[];
}

export interface ResponseItemDetail {
  message: string;
  level: string;
}

export interface ErrorRO {
  message: string;
}

export function instanceOfMultipleItemsResponse(object: any): object is MultipleItemsResponse {
  if (typeof object == 'string') return false;
  return 'message' in object && 'issues' in object && Array.isArray(object.issues);
}

export function instanceOfErrorRO(object: any): object is ErrorRO {
  if (typeof object == 'string') return false;
  return 'message' in object;
}
