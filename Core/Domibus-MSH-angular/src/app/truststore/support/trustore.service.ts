import {HttpClient} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';
import {TrustStoreEntry} from './trustore.model';
import * as FileSaver from 'file-saver';

/**
 * @Author Dussart Thomas
 * @Since 3.3
 */

@Injectable()
export class TrustStoreService {

  constructor(private http: HttpClient, private alertService: AlertService) {
  }

  getEntries(url): Promise<TrustStoreEntry[]> {
    return this.http.get<TrustStoreEntry[]>(url).toPromise();
  }

  uploadFile(url, props): Promise<string> {
    let input = new FormData();
    Object.keys(props).forEach(key => input.append(key, props[key]));
    return this.http.post<string>(url, input).toPromise();
  }

  /**
   * Local persister for the jks file
   * @param data
   */
  saveTrustStoreFile(data: any, filename: string = 'TrustStore.jks') {
    const blob = new Blob([data], {type: 'application/octet-stream'});
    FileSaver.saveAs(blob, filename, false);
  }

  removeCertificate(url: string, cert: any): Promise<string> {
    const deleteUrl = url.replace('alias', cert.name);
    return this.http.delete<string>(deleteUrl).toPromise();
  }

  reloadStore(url = 'rest/truststore/reset') {
    return this.http.post<any>(url, null).toPromise();
  }
}
