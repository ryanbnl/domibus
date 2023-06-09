import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AlertsResult} from './support/alertsresult';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from '../common/alert/alert.service';
import {ErrorStateMatcher, MatDialog, ShowOnDirtyErrorStateMatcher} from '@angular/material';
import {SecurityService} from '../security/security.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {ServerSortableListMixin} from '../common/mixins/sortable-list.mixin';
import 'rxjs-compat/add/operator/filter';
import {DialogsService} from '../common/dialogs/dialogs.service';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';
import {ServerPageableListMixin} from '../common/mixins/pageable-list.mixin';
import {ApplicationContextService} from '../common/application-context.service';
import {AlertsEntry} from './support/alertsentry';
import {ComponentName} from '../common/component-name-decorator';
import {Md2DateChange} from 'angular-md2';

@Component({
  moduleId: module.id,
  templateUrl: 'alerts.component.html',
})
@ComponentName('Alerts')
export class AlertsComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ServerSortableListMixin, ModifiableListMixin, ServerPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  static readonly ALERTS_URL: string = 'rest/alerts';
  static readonly ALERTS_CSV_URL: string = AlertsComponent.ALERTS_URL + '/csv';
  static readonly ALERTS_TYPES_URL: string = AlertsComponent.ALERTS_URL + '/types';
  static readonly ALERTS_STATUS_URL: string = AlertsComponent.ALERTS_URL + '/status';
  static readonly ALERTS_LEVELS_URL: string = AlertsComponent.ALERTS_URL + '/levels';
  static readonly ALERTS_PARAMS_URL: string = AlertsComponent.ALERTS_URL + '/params';

  TIME_SUFFIX = '_TIME';
  DATE_SUFFIX = '_DATE';
  IMMINENT_SUFFIX = '_IMMINENT';

  @ViewChild('rowProcessed', {static: false}) rowProcessed: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl', {static: false}) rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowWithFutureDateFormatTpl', {static: false}) rowWithFutureDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowWithSpaceAfterCommaTpl', {static: false}) rowWithSpaceAfterCommaTpl: TemplateRef<any>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;

  aTypes: Array<any>;
  aStatuses: Array<any>;
  aLevels: Array<any>;

  aProcessedValues = ['PROCESSED', 'UNPROCESSED'];

  dynamicFilters: Array<any>;
  dynamicDatesFilter: any;
  nonDateParameters: Array<any>;
  alertTypeWithDate: boolean;

  creationFromMaxDate: Date;
  creationToMinDate: Date;
  creationToMaxDate: Date;

  reportingFromMaxDate: Date;
  reportingToMinDate: Date;
  reportingToMaxDate: Date;

  dynamicDataFromMaxDate: Date;
  dynamicDataToMinDate: Date;
  dynamicDataToMaxDate: Date;

  dateFromName: string;
  dateToName: string;
  displayDomainCheckBox: boolean;
  areRowsDeleted: boolean;
  areRowsEdited: boolean;

  matcher: ErrorStateMatcher = new ShowOnDirtyErrorStateMatcher;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private alertService: AlertService,
              public dialog: MatDialog, private dialogsService: DialogsService,
              private securityService: SecurityService, private changeDetector: ChangeDetectorRef) {
    super();

    this.getAlertTypes();
    this.getAlertLevels();
    this.getAlertStatuses();
  }

  ngOnInit() {
    super.ngOnInit();

    this.dynamicFilters = [];
    this.dynamicDatesFilter = {};
    this.nonDateParameters = [];
    this.alertTypeWithDate = false;

    this.creationFromMaxDate = new Date();
    this.creationToMinDate = null;
    this.creationToMaxDate = new Date();

    this.reportingFromMaxDate = new Date();
    this.reportingToMinDate = null;
    this.reportingToMaxDate = new Date();

    this.dynamicDataFromMaxDate = new Date();
    this.dynamicDataToMinDate = null;
    this.dynamicDataToMaxDate = new Date();

    this.dateFromName = '';
    this.dateToName = '';
    this.displayDomainCheckBox = this.securityService.isCurrentUserSuperAdmin();

    super.filter = {processed: 'UNPROCESSED', domainAlerts: false};

    super.orderBy = 'creationTime';
    super.asc = false;
    this.areRowsDeleted = false;
    this.areRowsEdited = false;
    this.filterData();
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {name: 'Alert Id', width: 20, prop: 'entityId'},
      {name: 'Processed', cellTemplate: this.rowProcessed, width: 20},
      {name: 'Alert Type'},
      {name: 'Alert Level', width: 50},
      {name: 'Alert Status', width: 50},
      {name: 'Creation Time', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Reporting Time', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Parameters', cellTemplate: this.rowWithSpaceAfterCommaTpl, sortable: false},
      {name: 'Sent Attempts', width: 50, prop: 'attempts',},
      {name: 'Max Attempts', width: 50},
      {name: 'Next Attempt', cellTemplate: this.rowWithFutureDateFormatTpl, width: 155},
      {name: 'Reporting Time Failure', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Actions', cellTemplate: this.rowActions, width: 60, canAutoResize: true, sortable: false, showInitially: true}
    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Processed', 'Alert Type', 'Alert Level', 'Alert Status', 'Creation Time', 'Parameters', 'Actions'].indexOf(col.name) != -1
    });

  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  getAlertTypes(): void {
    this.aTypes = [];
    this.http.get<any[]>(AlertsComponent.ALERTS_TYPES_URL)
      .subscribe(aTypes => this.aTypes = aTypes);
  }

  getAlertStatuses(): void {
    this.aStatuses = [];
    this.http.get<any[]>(AlertsComponent.ALERTS_STATUS_URL)
      .subscribe(aStatuses => this.aStatuses = aStatuses);
  }

  getAlertLevels(): void {
    this.aLevels = [];
    this.http.get<any[]>(AlertsComponent.ALERTS_LEVELS_URL)
      .subscribe(aLevels => this.aLevels = aLevels);
  }

  protected get GETUrl(): string {
    return AlertsComponent.ALERTS_URL;
  }

  public setServerResults(result: AlertsResult) {
    super.count = result.count;
    super.rows = result.alertsEntries;
  }

  protected createAndSetParameters(): HttpParams {
    let filterParams = super.createAndSetParameters();

    if (this.activeFilter.processed) {
      filterParams = filterParams.set('processed', this.activeFilter.processed === 'PROCESSED' ? 'true' : 'false');
    }

    filterParams = this.setDynamicFilterParams(filterParams);

    return filterParams;
  }

  private setDynamicFilterParams(searchParams: HttpParams) {
    if (this.dynamicFilters.length > 0) {
      for (let filter of this.dynamicFilters) {
        searchParams = searchParams.append('parameters', filter || '');
      }
    }

    if (this.alertTypeWithDate) {
      const from = this.dynamicDatesFilter.from;
      if (from) {
        searchParams = searchParams.append('dynamicFrom', from.getTime());
      }

      const to = this.dynamicDatesFilter.to;
      if (to) {
        searchParams = searchParams.append('dynamicTo', to.getTime());
      }
    }
    return searchParams;
  }

  getAlertParameters(alertType: string): Promise<string[]> {
    let searchParams = new HttpParams();
    searchParams = searchParams.append('alertType', alertType);
    return this.http.get<string[]>(AlertsComponent.ALERTS_PARAMS_URL, {params: searchParams}).toPromise();
  }

  async onAlertTypeChanged(alertType: string) {
    this.nonDateParameters = [];
    this.alertTypeWithDate = false;
    this.dynamicFilters = [];
    this.dynamicDatesFilter = [];
    const alertParameters = await this.getAlertParameters(alertType);

    let nonDateParameters = alertParameters.filter((value) => {
      console.log('Value:' + value);
      return (value.search(this.TIME_SUFFIX) === -1 && value.search(this.DATE_SUFFIX) === -1)
    });
    this.nonDateParameters.push(...nonDateParameters);
    let dateParameters = alertParameters.filter((value) => {
      return value.search(this.TIME_SUFFIX) > 0 || value.search(this.DATE_SUFFIX) > 1
    });
    dateParameters.forEach(item => {
      this.dateFromName = item + ' FROM';
      this.dateToName = item + ' TO';
      this.alertTypeWithDate = true;
    });
    this.dynamicDataToMaxDate = this.getDynamicDataToMaxDate(alertType);
    this.dynamicDataFromMaxDate = this.getDynamicDataToMaxDate(alertType);
  }

  private getDynamicDataToMaxDate(alertType: string) {
    return this.isFutureAlert(alertType) ? null : new Date();
  }

  isFutureAlert(alertType: string): boolean {
    return alertType && alertType.includes(this.IMMINENT_SUFFIX);
  }

  onTimestampCreationFromChange(event) {
    this.creationToMinDate = event.value;
  }

  onTimestampCreationToChange(event) {
    this.creationFromMaxDate = event.value;
  }

  onTimestampReportingFromChange(event) {
    this.reportingToMinDate = event.value;
  }

  onTimestampReportingToChange(event) {
    this.reportingFromMaxDate = event.value;
  }

  onDynamicDataFromChange($event: Md2DateChange) {
    this.dynamicDataToMinDate = $event.value;
  }

  onDynamicDataToChange($event: Md2DateChange) {
    this.dynamicDataFromMaxDate = $event.value;
  }

  setIsDirty() {
    super.isChanged = this.areRowsDeleted || this.areRowsEdited;
  }

  delete() {
    this.deleteAlerts(this.selected);
  }

  buttonDeleteAction(row) {
    this.deleteAlerts([row]);
  }

  private deleteAlerts(alerts: AlertsEntry[]) {
    for (const itemToDelete of alerts) {
      itemToDelete.deleted = true;
    }

    super.selected = [];
    this.areRowsDeleted = true;
    this.setIsDirty();
  }

  async doSave(): Promise<any> {
    return this.http.put(AlertsComponent.ALERTS_URL, this.rows).toPromise()
      .then(() => this.loadServerData());
  }

  setProcessedValue(row) {
    this.areRowsEdited = true;
    this.setIsDirty();
  }

  get csvUrl(): string {
    // todo: add dynamic params for csv filtering, if requested
    return AlertsComponent.ALERTS_CSV_URL + '?' + this.createAndSetParameters();
  }

  canDelete() {
    return this.atLeastOneRowSelected() && this.notEveryRowIsDeleted() && !this.isBusy();
  }

  private notEveryRowIsDeleted() {
    return !this.selected.every(el => el.deleted);
  }

  private atLeastOneRowSelected() {
    return this.selected.length > 0;
  }

}
