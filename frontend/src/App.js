import React, { forwardRef } from "react";
import ReactDOM from "react-dom";
import MaterialTable from "material-table";
import Container from "@material-ui/core/Container";
import Grid from "@material-ui/core/Grid";
import ArrowUpward from "@material-ui/icons/ArrowUpward";
import Check from "@material-ui/icons/Check";
import ChevronLeft from "@material-ui/icons/ChevronLeft";
import { DropzoneArea } from "material-ui-dropzone";
import CloseIcon from "@material-ui/icons/Close";
import GetApp from "@material-ui/icons/GetApp";
import Chip from "@material-ui/core/Chip";
import Divider from "@material-ui/core/Divider";
import Checkbox from "@material-ui/core/Checkbox";
import IconButton from "@material-ui/core/IconButton";
import Tooltip from "@material-ui/core/Tooltip";
import ChevronRight from "@material-ui/icons/ChevronRight";
import Clear from "@material-ui/icons/Clear";
import TextField from "@material-ui/core/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Typography from "@material-ui/core/Typography";
import "react-sweet-progress/lib/style.css";
import Paper from "@material-ui/core/Paper";
import SvgIcon from "@material-ui/core/SvgIcon";
import { Button } from "@material-ui/core";
import Modal from "@material-ui/core/Modal";
import Backdrop from "@material-ui/core/Backdrop";
import Fade from "@material-ui/core/Fade";
import FilterList from "@material-ui/icons/FilterList";
import FirstPage from "@material-ui/icons/FirstPage";
import LastPage from "@material-ui/icons/LastPage";
import Remove from "@material-ui/icons/Remove";
import SaveAlt from "@material-ui/icons/SaveAlt";
import Search from "@material-ui/icons/Search";
import ViewColumn from "@material-ui/icons/ViewColumn";
import { Progress } from "react-sweet-progress";
import { database1 } from "./databases";
import "./App.css";

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      ConfigData: [{}],
      ConfigSettings: [],
      AlgorithmData: [{}],
      configName: "",
      addConfigModal: false,
      editCriteriaModal: false,
      addCriteriaModal: false,
      testSelected: false,
      buttonDisabled: true,
      configNameBool: true,
      criteriaNameBool: true,
      selectedDataId: "",
      selectedConfigId: "",
      selectedId: [],
      resultsModal: false,
      selectedRow: null,
      showPassword: false,
      editConfigModal: false,
      dataModalTitle: "",
      configModalTitle: "",
      criteriaModalTitle: "",
      selectedConfigRow: [],
      nextId: -1,
      resultsData1: [],
      dataSearchPercent: 0,
      resultsModal1: false,
      uploadModal: false,
      uploadName: "",
      criteriaModal: false,
      selectedAlgo: [],
      selectedAlgoId: "",
      criteriaName: "",
      criteriaSaved: false,
      fetchedData: []
    };
    this.handleClose = this.handleClose.bind(this);
  }
  
  handleResults1 = async () => {
    const selectedAlgorithm = this.state.AlgorithmData[this.state.selectedId[0] - 1];
    let returnArray = [];
    let currArray = [];
    for(let i = 0; i < selectedAlgorithm.criteria.length; i += 1) {
      currArray = [];
      for(let j = 0; j < selectedAlgorithm.criteria[i].selectedFields.length; j += 1) {
        if (selectedAlgorithm.criteria[i].selectedFields[j] === 1) {
          currArray.push(this.state.ConfigSettings[j].attr_code);
        }
        if (j === selectedAlgorithm.criteria[i].selectedFields.length - 1) {
          returnArray.push(currArray);
        }
    }
  }
  let string = JSON.stringify(returnArray);
  string = encodeURIComponent(string);
  fetch(`http://localhost:8080/deduplicate_merged?data=${string}`)
  .then(response => response.json())
  .then(data =>  {
    let newArray = []; 
    for(let i = 0; i < data.length; i += 1) {
    let newString = "";
    for (let j = 0; j < data[i].length; j += 1) {
      newString += (data[i][j] + ': ')
    }
    newString = newString.substring(0, newString.length - 2);
    let newObj = {
      match: newString
    }
    newArray.push(newObj);
  }
  this.setState({ resultsData1: newArray, resultsModal1: true })
  console.log(newArray);
});
  
  };

  handleUploadClose = () => {
    this.setState({ uploadModal: false });
  };

  handleCriteriaModal = () => {
    this.setState({ criteriaModal: false });
  };

  handleClose = () => {
    this.setState({
      addConfigModal: false,
      editConfigModal: false,
      addCriteriaModal: false,
      editCriteriaModal: false,
      resultsModal: false,
      resultsModal1: false,
      criteriaModal: false,
      resultsData1: []
    });
  };

  handleAlgoCancel = () => {
    const g = JSON.parse(localStorage.getItem('initialvalue'));
    this.setState({
      crtieriaModal: false,
      AlgorithmData: g,
      addConfigModal: false,
      editConfigModal: false,
      addCriteriaModal: false,
      editCriteriaModal: false,
      resultsModal: false,
      resultsModal1: false,
      criteriaModal: false,
    })
  }

  handleCriteriaClose = () => {
    this.setState({
      addConfigModal: false,
      editConfigModal: false,
      addCriteriaModal: false,
      editCriteriaModal: false,
    })
  }

  modalHandleClose = () => {
    this.setState({
      addCriteriaModal: false,
      editCriteriaModal: false
    });
  };

  handleNameChange = event => {
    this.setState({
      uploadName: event.target.value
    });
  };

  handleCriteriaChange = event => {
    this.setState({
      criteriaName: event.target.value
    });
  };

  handleModalClose = () => {
    this.setState({
      addConfigModal: false,
      editConfigModal: false,
      addCriteriaModal: false,
      editCriteriaModal: false,
      resultsModal: false,
      resultsModal1: true
    });
  };

  validationCheck = () => {
    if (this.state.uploadName.length === 0) {
      this.setState({ configNameBool: false });
    }
    if (this.state.criteriaName.length === 0) {
      this.setState({ criteriaNameBool: false });
    } else {
      this.setState({ criteriaNameBool: true });
    }
  };

  handleConfigSave = () => {
    let AlgorithmData = this.state.AlgorithmData;
    const newData = {
      name: this.state.uploadName,
      id: AlgorithmData[this.state.selectedAlgoId - 1].criteria.length + 1,
      selectedFields: this.state.selectedConfigRow
    };
    AlgorithmData[this.state.selectedAlgoId - 1].criteria.push(newData);
    this.setState({
      AlgorithmData,
      addCriteriaModal: false,
      editCriteriaModal: false,
      configNameBool: true
    });
  };

  handleAddAlgoAddConfigSave = () => {
    let ConfigData = this.state.ConfigData;
    const newData = {
      name: this.state.uploadName,
      id: ConfigData.length +1,
      selectedFields: this.state.selectedConfigRow
    };
   ConfigData.push(newData);
    this.setState({
      ConfigData,
      addCriteriaModal: false,
      editCriteriaModal: false,
      configNameBool: true
    });
  };

  handleAddAlgoEditConfigSave = () => {
    let ConfigData = this.state.ConfigData;
    ConfigData[this.state.selectedCriteriaId - 1].selectedFields = this.state.selectedConfigRow;
    ConfigData[this.state.selectedCriteriaId - 1].name = this.state.uploadName;
    this.setState({
      ConfigData,
      editCriteriaModal: false,
      configNameBool: true,
      criteriaSaved: true
    });
  }

  handleEditConfigSave = () => {
    let AlgorithmData = this.state.AlgorithmData;
    AlgorithmData[this.state.selectedAlgoId - 1].criteria[this.state.selectedCriteriaId - 1].selectedFields = this.state.selectedConfigRow;
    AlgorithmData[this.state.selectedAlgoId - 1].criteria[this.state.selectedCriteriaId - 1].name = this.state.uploadName;
    this.setState({
      AlgorithmData,
      editCriteriaModal: false,
      configNameBool: true,
      criteriaSaved: true
    });
  };

  handleEditAlgoSave = () => {
    let AlgorithmData = this.state.AlgorithmData;
    AlgorithmData[this.state.selectedAlgoId - 1].name = this.state.criteriaName;
    this.setState({
      AlgorithmData,
      criteriaModal: false,
      criteriaNameBool: true,
    });
  };

  handleAlgoSave = () => {
    let AlgorithmData = this.state.AlgorithmData; 
    let newAlgo = {
      name: this.state.criteriaName,
      id: this.state.selectedAlgoId + 1,
      criteria: this.state.ConfigData
    }
    AlgorithmData.push(newAlgo)
    this.setState({
      AlgorithmData,
      criteriaModal: false,
      criteriaNameBool: true,
    });
  }

  handleChange = (event, name) => {
    this.setState({ [name]: event.target.value, configNameBool: true }, () => {
      if (this.state.addDataModal || this.state.editDataModal) {
        this.checkValid();
      }
    });
    const newString = `${name.toLowerCase()}Valid`;
    if (event.target.value) {
      this.setState({ [newString]: true });
    }
  };

  download = (filename, text) => {
    let jsonData = `$@${filename}$@${text}`;
    jsonData = jsonData.replace(/,/g, " ");
    const element = document.createElement("a");
    element.setAttribute(
      "href",
      "data:application/json; charset=utf-8," + encodeURIComponent(jsonData)
    );
    element.setAttribute("download", filename);
    element.style.display = "none";
    document.body.appendChild(element);
    element.click();
  };

  getResultsDataString = rowData => {
    return (
      <div>
        FirstName: {rowData.data.FirstName}
        <br />
        LastName: {rowData.data.LastName}
        <br />
        Social Security Number: {rowData.data.SSN}
        <br />
        Date of Birth: {rowData.data.DOB}
      </div>
    );
  };

  componentDidMount() {
    let newArray = [];
    let gg = this.getConfigs(newArray);
    this.setState({
      ConfigSettings: gg,
      AlgorithmData: this.AlgorithmData
    });
  }

  AlgorithmData = [
    {
      name: "Algorithm 1",
      id: 1,
      criteria: [
        { name: "Special Criteria 1", id: 1, selectedFields: [0, 1, 0, 1] },
        { name: "Criteria 2", id: 2, selectedFields: [1, 1, 1, 1] },
        { name: "Criteria 3", id: 3, selectedFields: [1, 1, 0, 0] }
      ]
    },
    {
      name: "Algorithm 2",
      id: 2,
      criteria: [
        { name: "Test Criteria 1", id: 1, selectedFields: [0, 1, 0, 1, 0] },
        { name: "Criteria 2", id: 2, selectedFields: [1, 1, 1, 1, 0] }
      ]
    },
    {
      name: "Algorithm 3",
      id: 3,
      criteria: [
        { name: "Criteria 1", id: 1, selectedFields: [0, 1, 0, 1, 0] },
        { name: "Criteria 2", id: 2, selectedFields: [1, 1, 0, 1, 0] },
        { name: "Criteria 3", id: 3, selectedFields: [0, 1, 1, 0, 0] }
      ]
    }
  ];

  getModalColumns = () => [
    {
      title: "",
      field: "name",
      render: rowData => (
        rowData.database ? <span style = {{paddingLeft: "2rem"}}>{rowData.name}</span> : <span>{rowData.name}</span>
      )
    },
    
  ];

  getResults = () => [
    {
      title: "Matching Patients",
      field: "match",
    },
  ];

  handleUploadChange(files) {
    var file = files[0];
    var reader = new FileReader();
    reader.readAsText(file, "UTF-8");
    let name;
    let config;
    var that = this;
    reader.onload = function(evt) {
      let newString = evt.target.result.split("$@");
      name = newString[1];
      config = newString[2];
      that.updateState(name, config);
    };
  }

  updateState = (name, config) => {
    let configArray = [];
    const newConfig = config.replace(/ /g, "");
    if (newConfig) {
      for (let i = 0; i < newConfig.length; i++) {
        if (newConfig.charAt(i) === "1") {
          configArray.push(1);
        } else {
          configArray.push(0);
        }
      }
    }
    this.setState({
      uploadName: name,
      uploadModal: false,
      selectedConfigRow: configArray
    });
  };
  
  getConfigs = (newArray) => {
    fetch(`http://localhost:8080/get_dedup_flags`)
    .then(res => res.json())
    .then(
      (result) => {
        this.setState({ fetchedData: result }, () => {
          if (this.state.fetchedData) {
            for(let i = 0; i < this.state.fetchedData.length; i += 1) {
              if(this.state.fetchedData[i].parent === null) {
                let newData = {
                  attr_code: this.state.fetchedData[i].attr_code,
                  name: this.state.fetchedData[i].desc,
                  id: null,
                  database: this.state.fetchedData[i].parent
                }
                newArray.push(newData);
              } else {
                for(let j = 0; j < newArray.length; j += 1) {
                  if(newArray[j].attr_code === this.state.fetchedData[i].parent) {
                    let newData = {
                      attr_code: this.state.fetchedData[i].attr_code,
                      name: this.state.fetchedData[i].desc,
                      id: null,
                      database: this.state.fetchedData[i].parent
                    }
                    let firstArray = newArray.slice(0, j + 1);
                    const secondArray = newArray.slice(j + 1, newArray.length);
                    firstArray.push(newData);
                    newArray = firstArray.concat(secondArray);
                  }
                }
                for(let i = 0; i < newArray.length; i += 1) {
                  newArray[i].id = i;
                }
              }
          }
          }
          this.setState({ConfigSettings: newArray})
      });
      },
      (error) => {
        console.log("there was an error");
      }
    )
  }

  getTitle = rowData => {
    let returnString = "";
    let z = rowData.selectedFields;
    if (z) {
      let a = z.length;
      if (rowData) {
        for (let i = 0; i < a; i += 1) {
          if (rowData.selectedFields[i] === 1) {
            if (i === 0) {
              returnString = `${this.state.ConfigSettings[0].name}, `;
            } else {
              if (this.state.ConfigSettings[i]) {
                returnString = `${returnString}${
                  this.state.ConfigSettings[i].name
                }, `;
              }
            }
          }
        }
      }
    }
    returnString = returnString.slice(0, -2);
    return returnString;
  };

  getAlgoTitle = rowData => {
    let returnString = "";
    let z = rowData.criteria;
    if (z) {
      let a = z.length;
      if (rowData) {
        for (let i = 0; i < a; i += 1) {
          if (i !== a - 1) {
            returnString = `${returnString} ${rowData.criteria[i].name},`;
          } else {
            returnString = `${returnString} ${rowData.criteria[i].name}`;
          }
        }
      }
    }
    return returnString;
  };

  isValid = (name, bool) => {
    if (this.state[name]) {
      this.setState({
        [bool]: true
      });
    } else {
      this.setState({
        [bool]: false
      });
    }
  };

  render() {
    if (this.state.dataSearchPercent === 100) {
      setTimeout(
        function() {
          this.setState({
            resultsModal: false,
            resultsModal1: true,
            dataSearchPercent: 0,
            testAll: false
          });
        }.bind(this),
        1000
      );
    }

    const configColumns = [
      {
        title: "",
        field: "id",
        cellStyle: {
          width: ".5rem",
          paddingRight: "0",
          paddingLeft: "0"
        },
        render: rowData => <a />
      },
      { title: "", field: "name" },
      {
        title: "",
        field: "selectedFields",
        cellStyle: {
          textAlign: "end"
        },
        render: rowData => (
          <Tooltip title={this.getTitle(rowData)}>
            <SvgIcon>
              <path
                d="M12 4c-4.41 0-8 3.59-8 8s3.59 8 8 8 8-3.59 8-8-3.59-8-8-8zm1 14h-2v-2h2v2zm0-3h-2c0-3.25 3-3 3-5 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 2.5-3 2.75-3 5z"
                opacity=".1"
              />
              <path d="M11 16h2v2h-2zm1-14C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z" />
            </SvgIcon>
          </Tooltip>
        )
      }
    ];

    const algorithmColumns = [
      {
        title: "",
        field: "id",
        cellStyle: {
          width: ".5rem",
          paddingRight: "0",
          paddingLeft: "0"
        },
        render: rowData => (
          <Checkbox checked={this.state.selectedId.includes(rowData.id)} />
        )
      },
      { title: "", field: "name" },
      {
        title: "",
        field: "selectedFields",
        cellStyle: {
          textAlign: "end"
        },
        render: rowData => (
          <Tooltip title={this.getAlgoTitle(rowData)}>
            <SvgIcon>
              <path
                d="M12 4c-4.41 0-8 3.59-8 8s3.59 8 8 8 8-3.59 8-8-3.59-8-8-8zm1 14h-2v-2h2v2zm0-3h-2c0-3.25 3-3 3-5 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 2.5-3 2.75-3 5z"
                opacity=".1"
              />
              <path d="M11 16h2v2h-2zm1-14C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z" />
            </SvgIcon>
          </Tooltip>
        )
      }
    ];

    const tableIcons = {
      Add: forwardRef((props, ref) => (
        <SvgIcon {...props} ref={ref}>
          <path
            d="M5 19h14V5H5v14zm2-8h4V7h2v4h4v2h-4v4h-2v-4H7v-2z"
            opacity=".5"
            style={{ color: "green" }}
          />
          <path
            style={{ color: "black" }}
            d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-8-2h2v-4h4v-2h-4V7h-2v4H7v2h4z"
          />
        </SvgIcon>
      )),
      Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
      GetApp: forwardRef((props, ref) => <GetApp {...props} ref={ref} />),
      Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
      Delete: forwardRef((props, ref) => (
        <SvgIcon {...props} ref={ref}>
          <path d="M8 9h8v10H8z" opacity=".5" style={{ color: "red" }} />
          <path d="M15.5 4l-1-1h-5l-1 1H5v2h14V4zM6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9z" />
        </SvgIcon>
      )),
      DetailPanel: forwardRef((props, ref) => (
        <ChevronRight {...props} ref={ref} />
      )),
      Edit: forwardRef((props, ref) => (
        <SvgIcon {...props} ref={ref}>
          <path
            d="M5 18.08V19h.92l9.06-9.06-.92-.92z"
            opacity=".5"
            style={{ color: "yellow" }}
          />
          <path d="M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.2-.2-.45-.29-.71-.29s-.51.1-.7.29l-1.83 1.83 3.75 3.75 1.83-1.83zM3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM5.92 19H5v-.92l9.06-9.06.92.92L5.92 19z" />
        </SvgIcon>
      )),
      Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
      Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
      FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
      LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
      NextPage: forwardRef((props, ref) => (
        <ChevronRight {...props} ref={ref} />
      )),
      PreviousPage: forwardRef((props, ref) => (
        <ChevronLeft {...props} ref={ref} />
      )),
      ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
      Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
      SortArrow: forwardRef((props, ref) => (
        <ArrowUpward {...props} ref={ref} />
      )),
      ThirdStateCheck: forwardRef((props, ref) => (
        <Remove {...props} ref={ref} />
      )),
      ViewColumn: forwardRef((props, ref) => (
        <ViewColumn {...props} ref={ref} />
      ))
    };
    return (
      <Container>
        <Grid container spacing={3}>
          <Grid item xs={6}>
            <MaterialTable
              icons={tableIcons}
              columns={algorithmColumns}
              actions={[
                {
                  icon: () => (
                    <SvgIcon>
                      <path
                        d="M5 18.08V19h.92l9.06-9.06-.92-.92z"
                        opacity=".5"
                        style={{ color: "yellow" }}
                      />
                      <path d="M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.2-.2-.45-.29-.71-.29s-.51.1-.7.29l-1.83 1.83 3.75 3.75 1.83-1.83zM3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM5.92 19H5v-.92l9.06-9.06.92.92L5.92 19z" />
                    </SvgIcon>
                  ),
                  tooltip: "Edit Algorithm",
                  onClick: (event, rowData) => {
                    localStorage.setItem('initialvalue', JSON.stringify(this.state.AlgorithmData));
                    this.setState({
                      selectedAlgo: rowData,
                      ConfigData: rowData.criteria,
                      criteriaModal: true,
                      criteriaModalTitle: "Edit Algorithms",
                      selectedAlgoId: rowData.id,
                      criteriaName: rowData.name,
                    });
                  }
                },
                {
                  icon: () => (
                    <SvgIcon>
                      <path
                        d="M5 19h14V5H5v14zm2-8h4V7h2v4h4v2h-4v4h-2v-4H7v-2z"
                        opacity=".5"
                        style={{ color: "green" }}
                      />
                      <path
                        style={{ color: "black" }}
                        d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-8-2h2v-4h4v-2h-4V7h-2v4H7v2h4z"
                      />
                    </SvgIcon>
                  ),
                  tooltip: "Add Algorithm",
                  isFreeAction: true,
                  onClick: (event, rowData) => {
                    localStorage.setItem('initialvalue', JSON.stringify(this.state.AlgorithmData));
                    let AlgorithmData = this.state.AlgorithmData;
                    this.setState({
                      selectedAlgo: rowData,
                      ConfigData: [],
                      criteriaModal: true,
                      criteriaModalTitle: "Add Algorithm",
                      selectedAlgoId: this.state.AlgorithmData.length,
                      criteriaName: "",
                    });
                  }
                },
                {
                  icon: () => (
                    <SvgIcon>
                      <path
                        d="M12 4c-4.41 0-8 3.59-8 8s3.59 8 8 8 8-3.59 8-8-3.59-8-8-8zm1 14h-2v-2h2v2zm0-3h-2c0-3.25 3-3 3-5 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 2.5-3 2.75-3 5z"
                        opacity=".1"
                      />
                      <path d="M11 16h2v2h-2zm1-14C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z" />
                    </SvgIcon>
                  ),
                  tooltip:
                    "Displayed below are the current algorithms, you can add, edit, delete and select the algorithim to test on the database",
                  isFreeAction: true,
                  disabled: true
                }
              ]}
              data={this.state.AlgorithmData}
              title="Algorithms"
              localization={{
                header: {
                  actions: ""
                },
                body: {
                  editRow: {
                    deleteText:
                      "Are you sure you want to delete this Algorithm?"
                  }
                }
              }}
              editable={{
                onRowDelete: oldData =>
                  new Promise((resolve, reject) => {
                    setTimeout(() => {
                      {
                        let AlgorithmData = this.state.AlgorithmData;
                        let index;
                        for(let i = 0; i < AlgorithmData.length; i += 1) {
                          if (oldData.id === AlgorithmData[i].id) {
                              index = i;
                          }
                        }
                        AlgorithmData.splice(index, 1);
                        this.setState({ AlgorithmData }, () => resolve());
                      }
                      resolve();
                    }, 1000);
                  })
              }}
              options={{
                search: false,
                maxBodyHeight: "25rem",
                minBodyHeight: "25rem",
                sorting: true,
                rowStyle: rowData => ({
                  backgroundColor: this.state.selectedId.includes(rowData.id)
                    ? "#EEE"
                    : "#FFF"
                })
              }}
              onRowClick={(evt, selectedRow) => {
                let aa = null;
                if (this.state.selectedId) {
                  aa = this.state.selectedId;
                  const index = aa.indexOf(selectedRow.id);
                  if (index === -1) {
                    aa = [];
                    aa.push(selectedRow.id);
                  } else {
                    aa = [];
                  }
                }
                if (aa.length === 0) {
                  this.setState({ buttonDisabled: true });
                } else {
                  this.setState({ buttonDisabled: false });
                }
                this.setState({ selectedId: aa });
              }}
            />
          </Grid>
          <Modal
            open={this.state.criteriaModal}
            onClose={this.handleCriteriaModal}
            closeAfterTransition
            BackdropComponent={Backdrop}
            BackdropProps={{
              timeout: 500
            }}
            className={"Modal"}
          >
            <Fade in={this.state.criteriaModal}>
              <Paper className={"Paper"}>
                <Grid container>
                  <Grid item xs={12}>
                    <Typography className="Header" variant="h5">
                      {this.state.criteriaModalTitle}
                    </Typography>
                  </Grid>
                  <Grid
                    item
                    xs={12}
                    style={{ marginBottom: "2rem", marginTop: "2rem" }}
                  >
                      <FormControlLabel
                        classes={{ label: "FormControl", root: "Root" }}
                        style={{ paddingTop: "1rem" }}
                        control={
                          <TextField
                            style={{
                              paddingLeft: "2rem",
                              width: "100%",
                              marginRight: "2rem"
                            }}
                            value={this.state.criteriaName}
                            error={!this.state.criteriaNameBool}
                            onBlur={this.validationCheck}
                            onChange={this.handleCriteriaChange}
                            className={"Textfield"}
                            variant="outlined"
                          />
                        }
                        label="*Name:"
                        labelPlacement="start"
                      />
                    <Grid item xs={12}>
                      <Typography
                        style={{
                          color: "red",
                          paddingLeft: "7rem",
                          paddingBottom: "2rem"
                        }}
                      >
                        {this.state.criteriaNameBool ? "\xa0" : "Required"}
                      </Typography>
                    </Grid>
                    <MaterialTable
                      icons={tableIcons}
                      columns={configColumns}
                      actions={[
                        {
                          icon: () => (
                            <SvgIcon>
                              <path
                                d="M5 18.08V19h.92l9.06-9.06-.92-.92z"
                                opacity=".5"
                                style={{ color: "yellow" }}
                              />
                              <path d="M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.2-.2-.45-.29-.71-.29s-.51.1-.7.29l-1.83 1.83 3.75 3.75 1.83-1.83zM3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM5.92 19H5v-.92l9.06-9.06.92.92L5.92 19z" />
                            </SvgIcon>
                          ),
                          tooltip: "Edit Criteria",
                          onClick: (event, rowData) => {
                            this.setState({
                              selectedRow: rowData,
                              editCriteriaModal: true,
                              configModalTitle: "Edit Criteria",
                              selectedCriteriaId: rowData.id,
                              uploadName: rowData.name,
                              configNameBool: true,
                              selectedConfigRow: rowData.selectedFields
                            });
                          }
                        },
                        {
                          icon: () => (
                            <SvgIcon>
                              <path
                                d="M5 19h14V5H5v14zm2-8h4V7h2v4h4v2h-4v4h-2v-4H7v-2z"
                                opacity=".5"
                                style={{ color: "green" }}
                              />
                              <path
                                style={{ color: "black" }}
                                d="M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-8-2h2v-4h4v-2h-4V7h-2v4H7v2h4z"
                              />
                            </SvgIcon>
                          ),
                          tooltip: "Add Criteria",
                          isFreeAction: true,
                          onClick: (event, rowData) => {
                            this.setState({
                              addCriteriaModal: true,
                              configNameBool: true,
                              configModalTitle: "Add Criteria",
                              selectedRow: "",
                              selectedCriteriaId: this.state.nextId,
                              selectedConfigRow: [0, 0, 0, 0, 0, 0],
                              uploadName: ""
                            });
                          }
                        },
                        {
                          icon: () => <GetApp />,
                          onClick: (event, rowData) => {
                            this.download(rowData.name, rowData.selectedFields);
                          }
                        },
                        {
                          icon: () => (
                            <SvgIcon>
                              <path
                                d="M12 4c-4.41 0-8 3.59-8 8s3.59 8 8 8 8-3.59 8-8-3.59-8-8-8zm1 14h-2v-2h2v2zm0-3h-2c0-3.25 3-3 3-5 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 2.5-3 2.75-3 5z"
                                opacity=".1"
                              />
                              <path d="M11 16h2v2h-2zm1-14C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z" />
                            </SvgIcon>
                          ),
                          tooltip:
                            "Displayed below are the current criterias, you can add, edit, delete and select the criterias to test on the database",
                          isFreeAction: true,
                          disabled: true
                        }
                      ]}
                      data={this.state.ConfigData}
                      title="Criteria"
                      localization={{
                        header: {
                          actions: ""
                        },
                        body: {
                          editRow: {
                            deleteText:
                              "Are you sure you want to delete this Criteria?"
                          }
                        }
                      }}
                      editable={{
                        onRowDelete: oldData =>
                          new Promise((resolve, reject) => {
                            setTimeout(() => {
                              {
                                let ConfigData = this.state.ConfigData;
                                const index = ConfigData.indexOf(ConfigData);
                                ConfigData.splice(index, 1);
                                this.setState({ ConfigData }, () => resolve());
                              }
                              resolve();
                            }, 1000);
                          })
                      }}
                      options={{
                        doubleHorizontalScroll: true,
                        search: false,
                        maxBodyHeight: "25rem",
                        minBodyHeight: "25rem",
                        sorting: true
                      }}
                    />
                    <Grid item xs={12} className={"ButtonContainer"}>
                      <Button
                        className={"Button"}
                        onClick={this.handleAlgoCancel}
                        style={{ marginRight: ".5rem" }}
                        variant="contained"
                        color="secondary"
                      >
                        Cancel
                      </Button>
                      <Button
                        onClick={
                          this.state.criteriaModalTitle === "Add Algorithm"
                            ? this.handleAlgoSave
                            : this.handleEditAlgoSave
                        }
                        disabled={
                          this.state.editCriteriaModal
                            ? false
                            : !this.state.criteriaName.length ||
                              !this.state.criteriaNameBool
                        }
                        className={"Button"}
                        style={{
                          marginLeft: ".5rem",
                          marginRight: "1rem"
                        }}
                        variant="contained"
                        color="primary"
                      >
                        Save
                      </Button>
                    </Grid>
                  </Grid>
                </Grid>
              </Paper>
            </Fade>
          </Modal>
          <Grid item xs = {12}/>
          <Grid container style = {{alignItems:"center"}}>
          <Grid className="ButtonContainer1" item xs={3}>
            <Button
              disabled={this.state.buttonDisabled}
              onClick={this.handleResults1}
              className="Button1"
              variant="contained"
              color="primary"
              style={{ marginLeft: ".5rem" }}
            >
              Test Selected
            </Button>
          </Grid>
          </Grid>
        </Grid>
        <Modal
          open={
            false || this.state.editCriteriaModal || this.state.addCriteriaModal
          }
          onClose={this.modalHandleClose}
          closeAfterTransition
          BackdropComponent={Backdrop}
          BackdropProps={{
            timeout: 500
          }}
          className={"Modal"}
        >
          <Fade
            in={this.state.editCriteriaModal || this.state.addCriteriaModal}
          >
            <Paper className={"Paper"}>
              <Grid container>
                <Grid item xs={6}>
                  <Typography className="Header" variant="h5">
                    {this.state.configModalTitle}
                  </Typography>
                </Grid>
                <Grid
                  item
                  xs={6}
                  style={{
                    display: "flex",
                    justifyContent: "flex-end"
                  }}
                >
                  <Tooltip title="This is the add/edit criteria page, please give the criteria a name, and select at least one option to test on the database">
                    <SvgIcon>
                      <path
                        d="M12 4c-4.41 0-8 3.59-8 8s3.59 8 8 8 8-3.59 8-8-3.59-8-8-8zm1 14h-2v-2h2v2zm0-3h-2c0-3.25 3-3 3-5 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 2.5-3 2.75-3 5z"
                        opacity=".1"
                      />
                      <path d="M11 16h2v2h-2zm1-14C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z" />
                    </SvgIcon>
                  </Tooltip>
                </Grid>
                <Grid
                  item
                  xs={12}
                  style={{ paddingTop: "0rem", PaddingBottom: "5rem" }}
                >
                  <Divider />
                  <Divider />
                </Grid>
                <Grid
                  item
                  xs={12}
                  style={{ marginBottom: "2rem", marginTop: "2rem" }}
                >
                  <FormControlLabel
                    classes={{ label: "FormControl", root: "Root" }}
                    style={{ paddingTop: "1rem" }}
                    control={
                      <TextField
                        style={{
                          paddingLeft: "2rem",
                          width: "100%",
                          marginRight: "2rem"
                        }}
                        value={this.state.uploadName}
                        error={!this.state.configNameBool}
                        onBlur={this.validationCheck}
                        onChange={this.handleNameChange}
                        className={"Textfield"}
                        variant="outlined"
                      />
                    }
                    label="*Name:"
                    labelPlacement="start"
                  />

                  <Grid item xs={12}>
                    <Typography
                      style={{
                        color: "red",
                        paddingLeft: "7rem",
                        paddingBottom: "2rem"
                      }}
                    >
                      {this.state.configNameBool ? "\xa0" : "Required"}
                    </Typography>
                  </Grid>
                  <MaterialTable
                    style={{ elevation: 10 }}
                    options={{
                      selection: true,
                      search: true,
                      showTextRowsSelected: false,
                      maxBodyHeight: "20rem",
                      minBodyHeight: "20rem",
                      showSelectAllCheckbox: false,
                      showTitle: false,
                      searchFieldAlignment: "left",
                      searchFieldStyle: {
                        width: "100%",
                        paddingLeft: "1rem"
                      },
                      width: "100%",
                      selectionProps: rowData => ({
                        checked: this.state.selectedConfigRow[rowData.id] === 1,
                        color: "primary"
                      })
                    }}
                    onSelectionChange={(rows, event) => {
                      if (event) {
                        if (this.state.editConfigModal) {
                          const selectedConfigRow = this.state
                            .selectedConfigRow;
                          selectedConfigRow[event.tableData.id] = event
                            .tableData.checked
                            ? 1
                            : 0;
                          this.setState({ selectedConfigRow });
                        } else {
                          const selectedConfigRow = this.state
                            .selectedConfigRow;
                          selectedConfigRow[event.tableData.id] = event
                            .tableData.checked
                            ? 1
                            : 0;
                          this.setState({ selectedConfigRow });
                        }
                      }
                    }}
                    icons={tableIcons}
                    columns={this.getModalColumns()}
                    data={this.state.ConfigSettings}
                    
                  />
                </Grid>
                <Grid item xs={6} className={"ButtonContainerLeft"}>
                  {this.state.addConfigModal && (
                    <Button
                      onClick={() => this.setState({ uploadModal: true })}
                    >
                      Upload Criteria
                    </Button>
                  )}
                </Grid>

                <Grid item xs={6} className={"ButtonContainer"}>
                  <Button
                    className={"Button"}
                    onClick={this.handleCriteriaClose}
                    style={{ marginRight: ".5rem" }}
                    variant="contained"
                    color="secondary"
                  >
                    Cancel
                  </Button>
                  <Button
                    onClick={
                      this.state.configModalTitle === "Add Criteria"
                        ? (this.state.criteriaModalTitle === "Add Algorithm" ? this.handleAddAlgoAddConfigSave : this.handleConfigSave)
                        : (this.state.criteriaModalTitle === "Add Algorithm" ? this.handleAddAlgoEditConfigSave : this.handleEditConfigSave)
                    }
                    disabled={
                      this.state.editConfigModal
                        ? false
                        : !this.state.uploadName.length ||
                          !this.state.configNameBool
                    }
                    className={"Button"}
                    style={{
                      marginLeft: ".5rem",
                      marginRight: "1rem"
                    }}
                    variant="contained"
                    color="primary"
                  >
                    Save
                  </Button>
                </Grid>
              </Grid>
            </Paper>
          </Fade>
        </Modal>
        <Modal
          open={false || this.state.resultsModal1}
          onClose={this.handleClose}
          closeAfterTransition
          BackdropComponent={Backdrop}
          BackdropProps={{
            timeout: 500
          }}
          className={"Modal"}
        >
          <Fade in={this.state.resultsModal1}>
            <Paper className={"Paper"}>
              <Grid container>
                <Grid item xs={12}>
                  <MaterialTable
                    options={{
                      search: false,
                      showTextRowsSelected: false,
                      maxBodyHeight: "25rem",
                      minBodyHeight: "25rem",
                      showSelectAllCheckbox: false,
                      width: "100%"
                    }}
                    title="Resulting Match Pairs"
                    icons={tableIcons}
                    data={this.state.resultsData1}
                    columns = {this.getResults()}
                  />
                </Grid>
              </Grid>
            </Paper>
          </Fade>
        </Modal>
        <Modal
          open={false || this.state.uploadModal}
          onClose={this.handleUploadClose}
          closeAfterTransition
          BackdropComponent={Backdrop}
          BackdropProps={{
            timeout: 500
          }}
          className={"Modal"}
        >
          <Fade in={this.state.uploadModal}>
            <Paper className={"Paper"}>
              <Grid container>
                <Grid item xs={12}>
                  <DropzoneArea
                    acceptedFiles={["application/json"]}
                    onChange={this.handleUploadChange.bind(this)}
                  />
                </Grid>
              </Grid>
            </Paper>
          </Fade>
        </Modal>
      </Container>
    );
  }
}

export default App;
