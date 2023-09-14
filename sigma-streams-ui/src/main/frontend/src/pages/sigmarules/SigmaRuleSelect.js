import React from "react";
import IconButton from "@mui/material/IconButton";
import Tooltip from "@mui/material/Tooltip";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import { withStyles } from "tss-react/mui";
import { Link } from "react-router-dom";

const defaultToolbarSelectStyles = {
  iconButton: {
  },
  iconContainer: {
    marginRight: "24px",
  }
};

var selectedTitle = '';
class SigmaRuleSelect extends React.Component {
  constructor(props) {
    super(props);
    console.log(this.props);
    const dataIndex = this.props.selectedRows.data[0].dataIndex;
    console.log("Index: " + dataIndex);
    const title = this.props.displayData[dataIndex].data[0];
    console.log("Selected Title: " + title);
    selectedTitle = title;
  }

  handleRuleEdit = () => {
    
  };

  handleRuleDelete = () => {

  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.iconContainer}>
        <Tooltip title={"Edit Rule"}>
          <Link to="/app/sigmaruleeditor" state={{title: selectedTitle}}>
            <IconButton className={classes.iconButton} onClick={this.handleRuleEdit}>
              <EditIcon className={classes.icon} />
            </IconButton>
          </Link>
        </Tooltip>
        <Tooltip title={"Delete Rule"}>
          <IconButton className={classes.iconButton} onClick={this.handleRuleDelete}>
            <DeleteIcon className={classes.icon} />
          </IconButton>
        </Tooltip>
      </div>
    );
  }
}

export default withStyles(SigmaRuleSelect, defaultToolbarSelectStyles, { name: "CustomToolbarSelect" });